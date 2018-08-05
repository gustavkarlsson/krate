package se.gustavkarlsson.krate.core

import Reducer
import StatefulTransformer
import Watcher
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler

/**
 * Manages application (sub)state by accepting commands,
 * transforming them into any number of results,
 * which are then used to sequentially produce new state updates
 *
 * @param State The type representing the state of the store
 * @param Command Commands are issued to produce results
 * @param Result The result of a command used to produce a new state
 */
class Store<State : Any, Command : Any, Result : Any>
internal constructor(
    initialState: State,
    internal val transformers: List<StatefulTransformer<State, Command, Result>>,
    internal val reducers: List<Reducer<State, Result>>,
    internal val commandWatchers: List<Watcher<Command>>,
    internal val resultWatchers: List<Watcher<Result>>,
    internal val stateWatchers: List<Watcher<State>>,
    internal val errorWatchers: List<Watcher<Throwable>>,
    internal val observeScheduler: Scheduler?,
    internal val retryOnError: Boolean
) {

    /**
     * The current state of the store
     */
    @Volatile
    var currentState: State = initialState
        private set

    /**
     * Issue a command to the store for processing
     *
     * @param command the command to issue
     */
    fun issue(command: Command) = commands.accept(command)

    private val commands = PublishRelay.create<Command>().toSerialized()

    private val internalStates = commands
        .watchCommands()
        .transformToResults()
        .watchResults()
        .reduceToStates()
        .setCurrentState()
        .watchStates()
        .watchErrors()
        .retryIfEnabled()
        .replay(1)
        .autoConnect()

    private fun Observable<Command>.watchCommands(): Observable<Command> {
        return doOnNext { value ->
            commandWatchers.forEach { watch ->
                watch(value)
            }
        }
    }

    private fun Observable<Command>.transformToResults(): Observable<Result> {
        return compose(CompositeTransformer(transformers, ::currentState))
    }

    private fun Observable<Result>.watchResults(): Observable<Result> {
        return doOnNext { value ->
            resultWatchers.forEach { watch ->
                watch(value)
            }
        }
    }

    private fun Observable<Result>.reduceToStates(): Observable<State> {
        return serialize()
            .scanWith(::currentState, CompositeReducer(reducers))
    }

    private fun Observable<State>.setCurrentState(): Observable<State> {
        return doOnNext { state ->
            currentState = state
        }
    }

    private fun Observable<State>.watchStates(): Observable<State> {
        return doOnNext { value ->
            stateWatchers.forEach { watch ->
                watch(value)
            }
        }
    }

    private fun <T> Observable<T>.watchErrors(): Observable<T> {
        return doOnError { throwable ->
            errorWatchers.forEach { watch ->
                watch(throwable)
            }
        }
    }

    private fun <T> Observable<T>.retryIfEnabled(): Observable<T> {
        return retry { _ -> retryOnError }
    }

    /**
     * An observable stream of state updates produced by this store,
     * starting with the current state
     *
     * State updates will be observed on the observe [Scheduler] if one was specified.
     *
     * *Note: All subscribers share a single upstream subscription,
     * so there is no need to use publishing operators such as [Observable.publish].*
     * @return An [Observable] of [State] updates
     */
    val states: Observable<State> = internalStates
        .setObserver()

    private fun Observable<State>.setObserver(): Observable<State> {
        return observeScheduler?.let {
            observeOn(it)
        } ?: this
    }

    internal fun subscribeInternal() {
        internalStates.subscribe()
    }
}
