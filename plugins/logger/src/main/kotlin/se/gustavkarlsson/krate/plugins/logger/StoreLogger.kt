package se.gustavkarlsson.krate.plugins.logger

import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin

class StoreLogger<State : Any, Command : Any, Result : Any>(
    private val commandLogger: ((Command) -> Unit)? = null,
    private val resultLogger: ((Result) -> Unit)? = null,
    private val stateLogger: ((State) -> Unit)? = null
) : StorePlugin<State, Command, Result> {

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> {
        return if (commandLogger == null) {
            interceptors
        } else {
            interceptors + { it.doOnNext(commandLogger) }
        }
    }

    override fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> {
        return if (resultLogger == null) {
            interceptors
        } else {
            interceptors + { it.doOnNext(resultLogger) }
        }
    }

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> {
        return if (stateLogger == null) {
            interceptors
        } else {
            interceptors + { it.doOnNext(stateLogger) }
        }
    }
}
