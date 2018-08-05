package se.gustavkarlsson.krate.core.dsl

import StatefulTransformer
import StatelessTransformer
import Watcher
import se.gustavkarlsson.krate.core.StateIgnoringTransformer
import se.gustavkarlsson.krate.core.TypedTransformer
import se.gustavkarlsson.krate.core.TypedWatcher

class Commands<State : Any, Command : Any, Result : Any>
internal constructor() {
    internal val transformers = mutableListOf<StatefulTransformer<State, Command, Result>>()
    internal val watchers = mutableListOf<Watcher<Command>>()

    /**
     * Adds a stateful transformer to the store.
     *
     * A stateful transformer has access to the current state and converts commands to results.
     *
     * @param transform the transformer function
     */
    fun transformAllWithState(transform: StatefulTransformer<State, Command, Result>) {
        transformers += transform
    }

    /**
     * Adds a stateful typed transformer to the store.
     *
     * A stateful typed transformer has access to the current state and converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transform the transformer function
     */
    inline fun <reified C : Command> transformWithState(noinline transform: StatefulTransformer<State, C, Result>) {
        transformAllWithState(TypedTransformer(C::class, transform))
    }

    /**
     * Adds a stateless transformer to the store.
     *
     * A stateless transformer converts commands to results.
     *
     * @param transform the transformer function
     */
    fun transformAll(transform: StatelessTransformer<Command, Result>) {
        transformAllWithState(StateIgnoringTransformer(transform))
    }

    /**
     * Adds a stateless typed transformer to the store.
     *
     * A stateless transformer converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transform the transformer function
     */
    inline fun <reified C : Command> transform(noinline transform: StatelessTransformer<C, Result>) {
        transformWithState(StateIgnoringTransformer(transform))
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
