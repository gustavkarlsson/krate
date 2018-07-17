package se.gustavkarlsson.krate.core

/**
 * Creates a store
 */
fun <State : Any, Command : Any, Result : Any> buildStore(
    block: StoreBuilder<State, Command, Result>.() -> Unit
): Store<State, Command, Result> {
    return StoreBuilder<State, Command, Result>()
        .apply(block)
        .build()
        .apply(Store<*,*,*>::subscribeInternal)
}
