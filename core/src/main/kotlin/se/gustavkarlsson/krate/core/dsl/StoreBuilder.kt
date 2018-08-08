package se.gustavkarlsson.krate.core.dsl

import Reducer
import StateAwareTransformer
import Interceptor
import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Store

@StoreDsl
class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor() {
    private var initialState: State? = null
    private val transformers = mutableListOf<StateAwareTransformer<State, Command, Result>>()
    private val reducers = mutableListOf<Reducer<State, Result>>()
    private val commandInterceptors = mutableListOf<Interceptor<Command>>()
    private val resultInterceptors = mutableListOf<Interceptor<Result>>()
    private val stateInterceptors = mutableListOf<Interceptor<State>>()
    private var observeScheduler: Scheduler? = null

    /**
     * Configure commands
     *
     * @param block the code used to configure
     */
    fun commands(block: Commands<State, Command, Result>.() -> Unit) {
        Commands<State, Command, Result>()
            .also(block)
            .let {
                transformers += it.transformers
                commandInterceptors += it.interceptors
            }
    }

    /**
     * Configure results
     *
     * @param block the code used to configure
     */
    fun results(block: Results<State, Result>.() -> Unit) {
        Results<State, Result>()
            .also(block)
            .let {
                reducers += it.reducers
                resultInterceptors += it.interceptors
            }
    }

    /**
     * Configure states
     *
     * @param block the code used to configure
     */
    fun states(block: States<State>.() -> Unit) {
        States(initialState, observeScheduler)
            .also(block)
            .let {
                initialState = it.initial
                observeScheduler = it.observeScheduler
                stateInterceptors += it.interceptors
            }
    }

    internal fun build(): Store<State, Command, Result> {
        val initialState = checkNotNull(initialState) { "No initial state set" }
        check(!transformers.isEmpty()) { "No transformers defined" }
        check(!reducers.isEmpty()) { "No reducers defined" }
        return Store(
            initialState,
            transformers,
            reducers,
            commandInterceptors,
            resultInterceptors,
            stateInterceptors,
            observeScheduler
        )
    }
}
