package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.TypedWatcher
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for results.
 */
@StoreDsl
class Results<State : Any, Result : Any>
internal constructor() {
    internal var reducer: Reducer<State, Result>? = null
    internal val interceptors = mutableListOf<Interceptor<Result>>()

    /**
     * Sets the reducer for the store.
     *
     * A reducer takes the current state of the store and a result to produce a new state.
     *
     * @param reducer the reducer function
     */
    fun reduce(reducer: Reducer<State, Result>) {
        this.reducer = reducer
    }

    /**
     * Adds a result interceptor to the store.
     *
     * A result interceptor can add further processing to the stream of result
     *
     * @param interceptor the interceptor function
     */
    fun intercept(interceptor: Interceptor<Result>) {
        interceptors += interceptor
    }

    /**
     * Adds a watching result interceptor to the store.
     *
     * A watching result interceptor runs on each processed result
     *
     * @param watcher the watcher function
     */
    fun watchAll(watcher: Watcher<Result>) {
        intercept(WatchingInterceptor(watcher))
    }

    /**
     * Adds a typed watching result interceptor to the store.
     *
     * A typed watching result interceptor runs on each processed result of type [R]
     *
     * @param watcher the watcher function
     */
    inline fun <reified R : Result> watch(noinline watcher: Watcher<R>) {
        watchAll(TypedWatcher(R::class.java, watcher))
    }
}
