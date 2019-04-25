package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Store

/**
 * Creates a [Store] using the Krate DSL.
 */
fun <State : Any, Command : Any, Result : Any> buildStore(
    block: StoreBuilder<State, Command, Result>.(getState: () -> State) -> Unit
): Store<State, Command, Result> {
    val stateDelegate = StateDelegate<State>()
    return StoreBuilder<State, Command, Result>(stateDelegate)
        .apply { block(stateDelegate::valueUnsafe) }
        .build()
}
