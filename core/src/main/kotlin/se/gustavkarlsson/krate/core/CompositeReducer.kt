package se.gustavkarlsson.krate.core

internal class CompositeReducer<State : Any, Result : Any>(
    private val reducers: Iterable<Reducer<State, Result>>
) : Reducer<State, Result> {

    override fun invoke(currentState: State, result: Result): State {
        var newState = currentState
        reducers.forEach { reduce ->
            newState = reduce(newState, result)
        }
        return newState
    }
}
