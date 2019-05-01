package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.InterceptorWithReceiver
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.ReducerWithReceiver
import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.TypedReducer
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for states.
 */
@StoreDsl
class States<State : Any, Command : Any>
internal constructor(
    private val stateDelegate: StateDelegate<State>,

    /**
     * An optional scheduler that will be used to observe state changes.
     *
     * Default is *null*
     */
    var observeScheduler: Scheduler? = null
) {

    /**
     * The initial state of the store.
     */
    var initial: State?
        get() = stateDelegate.value
        set(value) {
            stateDelegate.value = value
        }

    internal val reducers = mutableListOf<Reducer<State, Command>>()

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a command to produce a new state.
     *
     * @param reducer the reducer function
     */
    fun reduceAll(reducer: ReducerWithReceiver<State, Command>) {
        reducers += reducer
    }

    /**
     * Adds a typed reducer to the store.
     *
     * A typed reducer takes the current state of the store and a command of type [R] to produce a new state.
     *
     * @param R the type of command to reduce
     * @param reducer the reducer function
     */
    inline fun <reified R : Command> reduce(noinline reducer: ReducerWithReceiver<State, R>) {
        reduceAll(TypedReducer(R::class.java, reducer))
    }

    internal val interceptors = mutableListOf<Interceptor<State>>()

    /**
     * Adds a state interceptor to the store.
     *
     * A state interceptor can add further processing to the stream of state
     *
     * @param interceptor the interceptor function
     */
    fun intercept(interceptor: InterceptorWithReceiver<State>) {
        interceptors += interceptor
    }

    /**
     * Adds a watching state interceptor to the store.
     *
     * A watching state interceptor runs on each processed state
     *
     * @param watcher the watcher function
     */
    fun watchAll(watcher: Watcher<State>) {
        intercept(WatchingInterceptor(watcher))
    }
}
