package se.gustavkarlsson.krate.core

import StateAwareTransformer
import StateIgnoringTransformer
import io.reactivex.Observable

internal class CompositeTransformer<State, Command, Result>(
    private val transformers: List<StateAwareTransformer<State, Command, Result>>,
    private val getCurrentState: () -> State
) : StateIgnoringTransformer<Command, Result> {

    override fun invoke(commands: Observable<Command>): Observable<Result> {
        return commands.publish {
            Observable.merge(it.splitAndTransform(transformers, getCurrentState))
        }
    }

    private fun Observable<Command>.splitAndTransform(
        transformers: List<StateAwareTransformer<State, Command, Result>>,
        getState: () -> State
    ): List<Observable<Result>> {
        return transformers
            .map { transform ->
                transform(this, getState)
            }
    }
}
