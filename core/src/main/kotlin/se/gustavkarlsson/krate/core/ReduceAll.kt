package se.gustavkarlsson.krate.core

internal fun <State : Any, Command : Any> Iterable<Reducer<State, Command>>.reduceAll(
    initialState: State,
    command: Command
): State = fold(initialState) { state, reduce -> reduce(state, command) }
