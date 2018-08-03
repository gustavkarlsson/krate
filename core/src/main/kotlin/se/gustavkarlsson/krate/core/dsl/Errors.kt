package se.gustavkarlsson.krate.core.dsl

import Watcher

class Errors
internal constructor(
    /**
     * Indicates whether to resubscribe whenever an error occurs instead of stopping the store.
     *
     * This can be useful in production environments but should be disabled during development.
     *
     * Default is *false*
     */
    var retry: Boolean
) {
    internal val watchers = mutableListOf<Watcher<Throwable>>()

    /**
     * Adds an error watcher to the store.
     *
     * An error watcher runs for each error encountered
     *
     * @param watch the watcher function
     */
    fun watch(watch: Watcher<Throwable>) {
        watchers += watch
    }
}
