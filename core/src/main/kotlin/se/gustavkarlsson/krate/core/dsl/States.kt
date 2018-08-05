package se.gustavkarlsson.krate.core.dsl

import Watcher
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
    internal val watchers = mutableListOf<Watcher<State>>()

    /**
     * Adds a state watcher to the store.
     *
     * A state watcher runs on each processed state
     *
     * @param watch the watcher function
     */
    fun watch(watch: Watcher<State>) {
        watchers += watch
    }
}
