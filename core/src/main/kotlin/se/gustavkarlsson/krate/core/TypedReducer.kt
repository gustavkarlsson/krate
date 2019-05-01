package se.gustavkarlsson.krate.core

/**
 * A reducer that filters commands of a specified type and applies the given reducer for that type to it.
 */
class TypedReducer<State : Any, Command : Any, C : Command>(
    private val type: Class<C>,
    private val reduce: Reducer<State, C>
) : Reducer<State, Command> {

    override fun invoke(state: State, command: Command): State {
        return command.mapIfInstanceOf(type) {
            reduce(state, it)
        } ?: state
    }
}
