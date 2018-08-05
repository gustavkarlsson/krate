package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Store

/**
 * Creates a store using the declarative DSL
 */
fun <State : Any, Command : Any, Result : Any> buildStore(
    block: StoreBuilder<State, Command, Result>.() -> Unit
): Store<State, Command, Result> {
    return StoreBuilder<State, Command, Result>()
        .apply(block)
        .build()
        .apply(Store<*, *, *>::subscribeInternal)
}
