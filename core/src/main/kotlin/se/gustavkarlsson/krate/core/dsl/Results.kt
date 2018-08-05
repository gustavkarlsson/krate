package se.gustavkarlsson.krate.core.dsl

import Reducer
import Watcher
import se.gustavkarlsson.krate.core.TypedReducer
import se.gustavkarlsson.krate.core.TypedWatcher

class Results<State : Any, Result : Any>
internal constructor() {
    internal val reducers = mutableListOf<Reducer<State, Result>>()
    internal val watchers = mutableListOf<Watcher<Result>>()

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a result to produce a new state.
     *
     * @param reduce the reducer function
     */
    fun reduceAll(reduce: Reducer<State, Result>) {
        reducers += reduce
    }

    /**
     * Adds a typed reducer to the store.
     *
     * A typed reducer takes the current state of the store and a result of type [R] to produce a new state.
     *
     * @param R the type of result to reduce
     * @param reduce the reducer function
     */
    inline fun <reified R : Result> reduce(noinline reduce: Reducer<State, R>) {
        reduceAll(TypedReducer(R::class, reduce))
    }

    /**
     * Adds a result watcher to the store.
     *
     * A result watcher runs on each processed command
     *
     * @param watch the watcher function
     */
    fun watchAll(watch: Watcher<Result>) {
        watchers += watch
    }

    /**
     * Adds a typed result watcher to the store.
     *
     * A typed result watcher runs on each processed result of type [R]
     *
     * @param watch the watcher function
     */
    inline fun <reified R : Result> watch(noinline watch: Watcher<R>) {
        watchAll(TypedWatcher(R::class, watch))
    }
}
