package se.gustavkarlsson.krate.core.dsl

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.InterceptorWithReceiver
import se.gustavkarlsson.krate.core.TransformerWithReceiver
import se.gustavkarlsson.krate.core.TypedInterceptor
import se.gustavkarlsson.krate.core.TypedWatcher
import se.gustavkarlsson.krate.core.Watcher
import se.gustavkarlsson.krate.core.WatchingInterceptor

/**
 * A configuration block for commands.
 */
@StoreDsl
class Commands<Command : Any>
internal constructor() {
    internal val interceptors = mutableListOf<Interceptor<Command>>()

    /**
     * Adds a typed transformer to the store.
     *
     * A transformer converts commands of type [C] to commands of type [Command], while ignoring other commands
     *
     * @param C the type of commands to transform
     * @param transformer the transformer function
     */
    fun <C : Command> transformTyped(type: Class<C>, transformer: TransformerWithReceiver<C, Command>) {
        interceptors += TypedInterceptor(type, transformer)
    }

    /**
     * Adds a typed transformer to the store.
     *
     * A transformer converts commands of type [C] to commands of type [Command], while ignoring other commands
     *
     * @param C the type of commands to transform
     * @param transformer the transformer function
     */
    inline fun <reified C : Command> transform(noinline transformer: TransformerWithReceiver<C, Command>) {
        transformTyped(C::class.java, transformer)
    }

    /**
     * Adds a command interceptor to the store.
     *
     * A command interceptor can add further processing to the stream of commands
     *
     * @param interceptor the interceptor function
     */
    fun intercept(interceptor: InterceptorWithReceiver<Command>) {
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
        watchAll(TypedWatcher(C::class.java, watcher))
    }
}
