package se.gustavkarlsson.krate.core.dsl

import Transformer
import Watcher
import se.gustavkarlsson.krate.core.TypedTransformer
import se.gustavkarlsson.krate.core.TypedWatcher

class Commands<State : Any, Command : Any, Result : Any>
internal constructor() {
    internal val transformers = mutableListOf<Transformer<State, Command, Result>>()
    internal val watchers = mutableListOf<Watcher<Command>>()

    /**
     * Adds a transformer to the store.
     *
     * A transformer converts commands to results.
     *
     * @param transform the transformer function
     */
    fun transformAll(transform: Transformer<State, Command, Result>) {
        transformers += transform
    }

    /**
     * Adds a typed transformer to the store.
     *
     * A typed transformer converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transform the transformer function
     */
    inline fun <reified C : Command> transform(noinline transform: Transformer<State, C, Result>) {
        transformAll(TypedTransformer(C::class, transform))
    }

    /**
     * Adds a command watcher to the store.
     *
     * A command watcher runs on each processed command
     *
     * @param watch the watcher function
     */
    fun watchAll(watch: Watcher<Command>) {
        watchers += watch
    }

    /**
     * Adds a typed command watcher to the store.
     *
     * A typed command watcher runs on each processed command of type [C]
     *
     * @param watch the watcher function
     */
    inline fun <reified C : Command> watch(noinline watch: Watcher<C>) {
        watchAll(TypedWatcher(C::class, watch))
    }
}
