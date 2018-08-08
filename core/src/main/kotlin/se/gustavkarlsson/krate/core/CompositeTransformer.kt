package se.gustavkarlsson.krate.core

import StateAwareTransformer
import StateIgnoringTransformer
import io.reactivex.Flowable

internal class CompositeTransformer<State, Command, Result>(
    private val transformers: List<StateAwareTransformer<State, Command, Result>>,
    private val getCurrentState: () -> State
) : StateIgnoringTransformer<Command, Result> {

    override fun invoke(commands: Flowable<Command>): Flowable<Result> {
        return commands.publish {
            Flowable.merge(it.splitAndTransform(transformers, getCurrentState))
        }
    }

    private fun Flowable<Command>.splitAndTransform(
        transformers: List<StateAwareTransformer<State, Command, Result>>,
        getState: () -> State
    ): List<Flowable<Result>> {
        return transformers
            .map { transform ->
                transform(this, getState)
            }
    }
}
