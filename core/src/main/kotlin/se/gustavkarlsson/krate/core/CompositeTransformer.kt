package se.gustavkarlsson.krate.core

import Transformer
import io.reactivex.Observable

class CompositeTransformer<State, Command, Result>(
    private val transformers: List<Transformer<State, Command, Result>>,
    private val getCurrentState: () -> State
) : (Observable<Command>) -> Observable<Result> {

    override fun invoke(commands: Observable<Command>): Observable<Result> {
        return commands.publish {
            Observable.merge(it.splitAndTransform(transformers, getCurrentState))
        }
    }

    private fun Observable<Command>.splitAndTransform(
        transformers: List<Transformer<State, Command, Result>>,
        getState: () -> State
    ): List<Observable<Result>> {
        return transformers
            .map { transform ->
                transform(this, getState)
            }
    }
}
