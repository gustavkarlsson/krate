package se.gustavkarlsson.krate.core

import Reducer
import Transformer
import Watcher
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

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
    internal val transformers: List<Transformer<State, Command, Result>>,
    internal val reducers: List<Reducer<State, Result>>,
    internal val commandWatchers: List<Watcher<Command>>,
    internal val resultWatchers: List<Watcher<Result>>,
    internal val stateWatchers: List<Watcher<State>>,
    internal val observeScheduler: Scheduler?
) {
    private var internalSubscription: Disposable? = null
    private var connection: Disposable? = null

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
     * @throws IllegalStateException if store has not been [start]ed
     */
    fun issue(command: Command) {
        if (internalSubscription == null) {
            throw IllegalStateException("Can't issue commands until started")
        }
        commands.accept(command)
    }

    private val commands = PublishRelay.create<Command>().toSerialized()

    private val results = commands
        .watch(commandWatchers)
        .publish { commands ->
            transformers
                .map { transform ->
                    commands.transform(::currentState)
                }
                .let {
                    Observable.merge(it)
                }
        }

    private val connectableStates = results
        .watch(resultWatchers)
        .serialize()
        .scan(initialState, CompositeReducer(reducers))
        .setCurrentState()
        .watch(stateWatchers)
        .observeOnIfPresent(observeScheduler)
        .replay(1)

    private fun Observable<State>.setCurrentState(): Observable<State> =
        doOnNext { state ->
            currentState = state
        }

    private fun <T> Observable<T>.observeOnIfPresent(scheduler: Scheduler?): Observable<T> {
        if (scheduler != null) {
            return this.observeOn(scheduler)
        }
        return this
    }

    private fun <T> Observable<T>.watch(watchers: List<Watcher<T>>): Observable<T> =
        this.doOnNext { value ->
            watchers.forEach { watch ->
                watch(value)
            }
        }

    /**
     * An observable stream of state updates produced by this store,
     * starting with the current state
     *
     * State updates will be observed on the observe [Scheduler] if one was specified
     * or a per-store unique background thread.
     *
     * *Note: The store will not produce any state until [start]ed.*
     *
     * *Note: All subscribers share a single upstream subscription,
     * so there is no need to use publishing operators such as [Observable.publish].*
     * @return An [Observable] of [State] updates
     */
    val states: Observable<State> = connectableStates

    /**
     * Starts processing of this store.
     * When started, commands issued via [issue] will be accepted
     * and any observer of [states] will start receiving state updates.
     */
    @Synchronized
    fun start() {
        if (isRunning) return
        connection = connectableStates.connect()
        internalSubscription = connectableStates.subscribe()
    }

    /**
     * Stops processing of this store.
     * When stopped, commands will no longer be accepted
     * and [states] will stop producing state updates
     */
    @Synchronized
    fun stop() {
        if (!isRunning) return
        internalSubscription?.dispose()
        connection?.dispose()
        internalSubscription = null
        connection = null
    }

    /**
     * Is this store running (has been [start]ed)?
     */
    val isRunning get() = internalSubscription != null
}
