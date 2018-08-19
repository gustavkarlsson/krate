package se.gustavkarlsson.krate.plugins.logger

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin

class StoreLogger<State : Any, Command : Any, Result : Any>(
    private val commandLogger: ((Command) -> Unit)? = null,
    private val resultLogger: ((Result) -> Unit)? = null,
    private val stateLogger: ((State) -> Unit)? = null
) : StorePlugin<State, Command, Result> {

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> {
        return interceptors.appendIfNotNull(commandLogger)
    }

    override fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> {
        return interceptors.appendIfNotNull(resultLogger)
    }

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> {
        return interceptors.appendIfNotNull(stateLogger)
    }

    private fun <T> List<Interceptor<T>>.appendIfNotNull(consumer: ((T) -> Unit)?): List<Interceptor<T>> {
        return if (consumer == null) {
            this
        } else {
            this + { it.doOnNext(consumer) }
        }
    }
}
