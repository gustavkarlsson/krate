package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.TypedReducer
import se.gustavkarlsson.krate.core.TypedWatcher
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for results.
 */
@StoreDsl
class Results<State : Any, Result : Any>
internal constructor() {
    internal val reducers = mutableListOf<Reducer<State, Result>>()
    internal val interceptors = mutableListOf<Interceptor<Result>>()

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a result to produce a new state.
     *
     * @param reducer the reducer function
     */
    fun reduceAll(reducer: Reducer<State, Result>) {
        reducers += reducer
    }

    /**
     * Adds a typed reducer to the store.
     *
     * A typed reducer takes the current state of the store and a result of type [R] to produce a new state.
     *
     * @param R the type of result to reduce
     * @param reducer the reducer function
     */
    inline fun <reified R : Result> reduce(noinline reducer: Reducer<State, R>) {
        reduceAll(TypedReducer(R::class.java, reducer))
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
