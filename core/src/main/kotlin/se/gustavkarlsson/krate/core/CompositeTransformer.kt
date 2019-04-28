package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

internal class CompositeTransformer<Command, Result>(
    private val transformers: List<Transformer<Command, Result>>
) : Transformer<Command, Result> {

    override fun invoke(commands: Flowable<Command>): Flowable<Result> {
        return commands.publish {
            Flowable.merge(it.splitAndTransform(transformers))
        }
    }

    private fun Flowable<Command>.splitAndTransform(
        transformers: List<Transformer<Command, Result>>
    ): List<Flowable<Result>> {
        return transformers
            .map { transform ->
                transform(this)
            }
    }
}
