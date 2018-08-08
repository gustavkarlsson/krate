package se.gustavkarlsson.krate.core

import StateAwareTransformer
import StateIgnoringTransformer
import io.reactivex.Flowable

/**
 * A transformer that does not have access to state
 */
class Transformer<Command : Any, Result : Any>(
    private val transform: StateIgnoringTransformer<Command, Result>
) : StateAwareTransformer<Any, Command, Result> {

    override fun invoke(commands: Flowable<Command>, getState: () -> Any): Flowable<Result> {
        return transform(commands)
    }
}
