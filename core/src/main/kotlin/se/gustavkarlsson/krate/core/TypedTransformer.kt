package se.gustavkarlsson.krate.core

import StateAwareTransformer
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * A transformer that filters commands of a specified type and applies another transformer for that type to it.
 */
class TypedTransformer<State : Any, Command : Any, Result : Any, C : Command>(
    private val type: KClass<C>,
    private val transform: StateAwareTransformer<State, C, Result>
) : StateAwareTransformer<State, Command, Result> {

    override fun invoke(commands: Flowable<Command>, getState: () -> State): Flowable<Result> {
        return transform(commands.ofType(type.javaObjectType), getState)
    }
}
