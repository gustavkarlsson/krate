package se.gustavkarlsson.krate.core

import StatefulTransformer
import StatelessTransformer
import io.reactivex.Observable

/**
 * A transformer that does not have access to state
 */
class StateIgnoringTransformer<Command : Any, Result : Any>(
    private val transform: StatelessTransformer<Command, Result>
) : StatefulTransformer<Any, Command, Result> {

    override fun invoke(commands: Observable<Command>, getState: () -> Any): Observable<Result> {
        return transform(commands)
    }
}
