package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.StateAwareTransformer
import se.gustavkarlsson.krate.core.StateIgnoringTransformer
import se.gustavkarlsson.krate.core.Transformer
import se.gustavkarlsson.krate.core.TypedTransformer
import se.gustavkarlsson.krate.core.TypedWatcher
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for commands.
 */
@StoreDsl
class Commands<State : Any, Command : Any, Result : Any>
internal constructor() {
    internal val transformers = mutableListOf<StateAwareTransformer<State, Command, Result>>()
    internal val interceptors = mutableListOf<Interceptor<Command>>()

    /**
     * Adds a state aware transformer to the store.
     *
     * A state aware transformer has access to the current state and converts commands to results.
     *
     * @param transformer the transformer function
     */
    fun transformAllWithState(transformer: StateAwareTransformer<State, Command, Result>) {
        transformers += transformer
    }

    /**
     * Adds a state aware typed transformer to the store.
     *
     * A state aware typed transformer has access to the current state and converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transformer the transformer function
     */
    inline fun <reified C : Command> transformWithState(noinline transformer: StateAwareTransformer<State, C, Result>) {
        transformAllWithState(TypedTransformer(C::class, transformer))
    }

    /**
     * Adds a transformer to the store.
     *
     * A transformer converts commands to results.
     *
     * @param transformer the transformer function
     */
    fun transformAll(transformer: StateIgnoringTransformer<Command, Result>) {
        transformAllWithState(Transformer(transformer))
    }

    /**
     * Adds a typed transformer to the store.
     *
     * A transformer converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transformer the transformer function
     */
    inline fun <reified C : Command> transform(noinline transformer: StateIgnoringTransformer<C, Result>) {
        transformWithState(Transformer(transformer))
    }

    /**
     * Adds a command interceptor to the store.
     *
     * A command interceptor can add further processing to the stream of commands
     *
     * @param interceptor the interceptor function
     */
    fun intercept(interceptor: Interceptor<Command>) {
        interceptors += interceptor
    }

    /**
     * Adds a watching command interceptor to the store.
     *
     * A watching command interceptor runs on each processed command
     *
     * @param watcher the watcher function
     */
    fun watchAll(watcher: Watcher<Command>) {
        intercept(WatchingInterceptor(watcher))
    }

    /**
     * Adds a typed watching command interceptor to the store.
     *
     * A typed watching command interceptor runs on each processed command of type [C]
     *
     * @param watcher the watcher function
     */
    inline fun <reified C : Command> watch(noinline watcher: Watcher<C>) {
        watchAll(TypedWatcher(C::class, watcher))
    }
}
