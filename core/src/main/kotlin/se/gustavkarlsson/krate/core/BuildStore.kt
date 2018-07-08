package se.gustavkarlsson.krate.core

fun <State : Any, Command : Any, Result : Any> buildStore(
    block: StoreBuilder<State, Command, Result>.() -> Unit
): Store<State, Command, Result> {
    val builder = StoreBuilder<State, Command, Result>()
    builder.block()
    return builder.build()
}
