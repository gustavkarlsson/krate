package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

typealias Transformer<T, R> = (Flowable<T>) -> Flowable<R>
typealias TransformerWithReceiver<T, R> = Flowable<T>.() -> Flowable<R>

typealias Reducer<State, T> = (State, T) -> State
typealias ReducerWithReceiver<State, T> = State.(T) -> State

typealias Interceptor<T> = (Flowable<T>) -> Flowable<T>
typealias InterceptorWithReceiver<T> = Flowable<T>.() -> Flowable<T>

typealias Watcher<T> = (T) -> Unit
