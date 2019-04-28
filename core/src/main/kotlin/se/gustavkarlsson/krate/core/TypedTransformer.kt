package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

/**
 * A transformer that filters commands of a specified type and applies another transformer for that type to it.
 */
class TypedTransformer<Command : Any, Result : Any, C : Command>(
    private val type: Class<C>,
    private val transform: Transformer<C, Result>
) : Transformer<Command, Result> {

    override fun invoke(commands: Flowable<Command>): Flowable<Result> {
        return transform(commands.ofType(type))
    }
}
