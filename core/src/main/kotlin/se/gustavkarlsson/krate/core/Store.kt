package se.gustavkarlsson.krate.core

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Manages state by accepting commands, transforming them into results,
 * which are then used to sequentially produce new state updates.
 *
 * @param State The type representing the state of the store
 * @param Command Commands are issued to produce results
 * @param Result The result of a command used to produce a new state
 */
class Store<State : Any, Command : Any, Result : Any>
internal constructor(
    stateDelegate: StateDelegate<State>,
    internal val transformers: List<Transformer<Command, Result>>,
    internal val reducers: List<Reducer<State, Result>>,
    internal val commandInterceptors: List<Interceptor<Command>>,
    internal val resultInterceptors: List<Interceptor<Result>>,
    internal val stateInterceptors: List<Interceptor<State>>,
    internal val observeScheduler: Scheduler?
) : Disposable {

    /**
     * The current state of the store
     */
    var currentState by stateDelegate
        private set

    /**
     * Issues a command to the store for processing
     *
     * @param command the command to issue
     */
    fun issue(command: Command) = commands.onNext(command)

    private val commands = PublishSubject.create<Command>().toSerialized()

    private val internalStates = commands
        .toFlowable(BackpressureStrategy.MISSING)
        .onBackpressureBuffer(true)
        .intercept(commandInterceptors)
        .transformToResults()
        .intercept(resultInterceptors)
        .reduceToStates()
        .intercept(stateInterceptors)
        .onBackpressureLatest()
        .setCurrentState()
        .replay(1)

    private fun <T> Flowable<T>.intercept(interceptors: List<Interceptor<T>>): Flowable<T> {
        return interceptors.fold(this) { stream, intercept -> intercept(stream) }
    }

    private fun Flowable<Command>.transformToResults(): Flowable<Result> {
        return compose(CompositeTransformer(transformers))
    }

    private fun Flowable<Result>.reduceToStates(): Flowable<State> {
        return serialize().scanWith(::currentState, CompositeReducer(reducers))
    }

    private fun Flowable<State>.setCurrentState(): Flowable<State> {
        return doOnNext { state ->
            currentState = state
        }
    }

    /**
     * A stream of state updates produced by this store,
     * starting with the current state
     *
     * State updates will be observed on the observe [Scheduler] if one was specified.
     *
     * *Note: All subscribers share a single upstream subscription,
     * so there is no need to use publishing operators such as [Flowable.publish].*
     *
     * @return A stream of [State] updates
     */
    val states: Flowable<State> = internalStates
        .setObserver()

    private fun Flowable<State>.setObserver(): Flowable<State> {
        return observeScheduler?.let {
            observeOn(it)
        } ?: this
    }

    private val disposable: Disposable = CompositeDisposable(internalStates.subscribe(), internalStates.connect())

    override fun dispose() {
        disposable.dispose()
    }

    override fun isDisposed() = disposable.isDisposed
}
