package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

typealias StateIgnoringTransformer<Command, Result> = (Flowable<Command>) -> Flowable<Result>

typealias StateAwareTransformer<State, Command, Result> = (Flowable<Command>, () -> State) -> Flowable<Result>

typealias Reducer<State, Result> = (State, Result) -> State

typealias Interceptor<Type> = (Flowable<Type>) -> Flowable<Type>

typealias Watcher<Type> = (Type) -> Unit
