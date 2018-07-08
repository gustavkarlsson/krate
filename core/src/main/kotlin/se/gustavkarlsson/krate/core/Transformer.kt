package se.gustavkarlsson.krate.core

import io.reactivex.Observable

internal typealias Transformer<State, Command, Result> = (Observable<Command>, () -> State) -> Observable<Result>
