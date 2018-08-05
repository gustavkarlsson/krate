package se.gustavkarlsson.krate.core

import StatefulTransformer
import StatelessTransformer
import io.reactivex.Observable

class CompositeTransformer<State, Command, Result>(
    private val transformers: List<StatefulTransformer<State, Command, Result>>,
    private val getCurrentState: () -> State
) : StatelessTransformer<Command, Result> {

    override fun invoke(commands: Observable<Command>): Observable<Result> {
        return commands.publish {
            Observable.merge(it.splitAndTransform(transformers, getCurrentState))
        }
    }

    private fun Observable<Command>.splitAndTransform(
        transformers: List<StatefulTransformer<State, Command, Result>>,
        getState: () -> State
    ): List<Observable<Result>> {
        return transformers
            .map { transform ->
                transform(this, getState)
            }
    }
}
