package se.gustavkarlsson.krate.core

import Reducer
import kotlin.reflect.KClass

/**
 * A reducers that filters results of a specified type and applies another reducer for that type to it.
 */
class TypedReducer<State : Any, Result : Any, R : Result>(
    private val type: KClass<R>,
    private val reduce: Reducer<State, R>
) : Reducer<State, Result> {

    override fun invoke(state: State, result: Result): State {
        return if (type.javaObjectType.isInstance(result)) {
            @Suppress("UNCHECKED_CAST")
            reduce(state, result as R)
        } else {
            state
        }
    }
}
