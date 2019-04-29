package se.gustavkarlsson.krate.core

internal class CompositeReducer<State : Any, Result : Any>(
    private val reducers: Iterable<Reducer<State, Result>>
) : Reducer<State, Result> {

    override fun invoke(currentState: State, result: Result): State {
        return reducers.fold(currentState) { state, reduce ->
            reduce(state, result)
        }
    }
}
