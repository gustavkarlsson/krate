package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateAwareTransformer
import se.gustavkarlsson.krate.core.Store

/**
 * A configuration block for a [Store].
 */
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
     * Configures commands for the store.
     *
     * At least one transformer must be defined.
     *
     * @param block the code used to configure commands
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
     * Configures results for the store.
     *
     * At least one reducer must be defined.
     *
     * @param block the code used to configure results
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
     * Configures states for the store.
     *
     * [States.initial] must be set in at least one states block.
     *
     * @param block the code used to configure states
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
        val initialState = checkNotNull(initialState) {
            "No initial state set. Set the initial state in a states-block in the DSL"
        }
        check(!transformers.isEmpty()) {
            "No transformers defined. Add transformers in a commands-block in the DSL"
        }
        check(!reducers.isEmpty()) {
            "No reducers defined. Add reducers in a results-block in the DSL"
        }
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
