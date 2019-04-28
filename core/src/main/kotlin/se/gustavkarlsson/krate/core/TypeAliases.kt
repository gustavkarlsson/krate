package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

typealias Transformer<Command, Result> = (Flowable<Command>) -> Flowable<Result>

typealias Reducer<State, Result> = (State, Result) -> State

typealias Interceptor<Type> = (Flowable<Type>) -> Flowable<Type>

typealias Watcher<Type> = (Type) -> Unit
