package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for states.
 */
@StoreDsl
class States<State : Any>
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

    internal val interceptors = mutableListOf<Interceptor<State>>()

    /**
     * Adds a state interceptor to the store.
     *
     * A state interceptor can add further processing to the stream of state
     *
     * @param interceptor the interceptor function
     */
    fun intercept(interceptor: Interceptor<State>) {
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
