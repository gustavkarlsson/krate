package se.gustavkarlsson.krate.core.dsl

import Interceptor
import io.reactivex.Scheduler

class States<State : Any>
internal constructor(
    /**
     * The initial state of the store.
     */
    var initial: State? = null,

    /**
     * An optional scheduler that will be used to observe state changes.
     *
     * Default is *null*
     */
    var observeScheduler: Scheduler? = null
) {
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
}
