package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Store

/**
 * Creates a [Store] using the Krate DSL.
 */
fun <State : Any, Command : Any, Result : Any> buildStore(
    block: StoreBuilder<State, Command, Result>.() -> Unit
): Store<State, Command, Result> {
    return StoreBuilder<State, Command, Result>()
        .apply(block)
        .build()
        .apply(Store<*, *, *>::subscribeInternal)
}
