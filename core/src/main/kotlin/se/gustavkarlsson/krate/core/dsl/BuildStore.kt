package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Store

/**
 * Creates a [Store] using the Krate DSL.
 */
fun <State : Any, Command : Any> buildStore(
    block: StoreBuilder<State, Command>.(getState: () -> State) -> Unit
): Store<State, Command> {
    val stateDelegate = StateDelegate<State>()
    return StoreBuilder<State, Command>(stateDelegate)
        .apply { block(stateDelegate::valueUnsafe) }
        .build()
}
