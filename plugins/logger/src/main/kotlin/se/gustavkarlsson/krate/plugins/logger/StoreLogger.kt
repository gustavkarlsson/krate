package se.gustavkarlsson.krate.plugins.logger

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin

class StoreLogger<State : Any, Command : Any>(
    private val commandLogger: ((Command) -> Unit)? = null,
    private val stateLogger: ((State) -> Unit)? = null
) : StorePlugin<State, Command> {

    override fun changeCommandInterceptors(
        interceptors: List<Interceptor<Command>>,
        getState: () -> State
    ): List<Interceptor<Command>> = interceptors.appendIfNotNull(commandLogger)

    override fun changeStateInterceptors(
        interceptors: List<Interceptor<State>>,
        getState: () -> State
    ): List<Interceptor<State>> = interceptors.appendIfNotNull(stateLogger)

    private fun <T> List<Interceptor<T>>.appendIfNotNull(consumer: ((T) -> Unit)?): List<Interceptor<T>> =
        if (consumer == null) {
            this
        } else {
            this + { it.doOnNext(consumer) }
        }
}
