package se.gustavkarlsson.krate.core

import StatefulTransformer
import io.reactivex.Observable
import kotlin.reflect.KClass

/**
 * A transformer that filters commands of a specified type and applies another transformer for that type to it.
 */
class TypedTransformer<State : Any, Command : Any, Result : Any, C : Command>(
    private val type: KClass<C>,
    private val transform: StatefulTransformer<State, C, Result>
) : StatefulTransformer<State, Command, Result> {

    override fun invoke(commands: Observable<Command>, getState: () -> State): Observable<Result> {
        return transform(commands.ofType(type.javaObjectType), getState)
    }
}
