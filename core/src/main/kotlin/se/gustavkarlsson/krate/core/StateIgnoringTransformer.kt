package se.gustavkarlsson.krate.core

import StatefulTransformer
import StatelessTransformer
import io.reactivex.Observable

/**
 * A transformer that does not have access to state
 */
class StateIgnoringTransformer<State : Any, Command : Any, Result : Any>(
    private val transform: StatelessTransformer<Command, Result>
) : StatefulTransformer<State, Command, Result> {

    override fun invoke(commands: Observable<Command>, getState: () -> State): Observable<Result> {
        return transform(commands)
    }
}
