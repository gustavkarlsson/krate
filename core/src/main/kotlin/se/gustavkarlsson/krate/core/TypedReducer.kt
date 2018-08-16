package se.gustavkarlsson.krate.core

import kotlin.reflect.KClass

/**
 * A reducer that filters results of a specified type and applies another reducer for that type to it.
 */
class TypedReducer<State : Any, Result : Any, R : Result>(
    private val type: KClass<R>,
    private val reduce: Reducer<State, R>
) : Reducer<State, Result> {

    override fun invoke(state: State, result: Result): State {
        return result.ifObjectInstanceOf(type) {
            reduce(state, it)
        } ?: state
    }
}
