package se.gustavkarlsson.krate.core

internal class CompositeReducer<State : Any, Result : Any>(
    private val reducers: Iterable<Reducer<State, Result>>
) : Reducer<State, Result> {

    @Suppress("UNCHECKED_CAST")
    override fun invoke(currentState: State, result: Result): State {
        var newState = currentState
        reducers
            .forEach {
                newState = it(newState, result)
            }

        return newState
    }
}
