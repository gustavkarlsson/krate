package se.gustavkarlsson.krate.core

internal typealias Reducer<State, Result> = (State, Result) -> State
