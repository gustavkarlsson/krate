package se.gustavkarlsson.krate.core.dsl

import Interceptor
import Reducer
import StateAwareTransformer
import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Store

@StoreDsl
class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor() {
    private var initialState: State? = null
    private var transformers = mutableListOf<StateAwareTransformer<State, Command, Result>>()
    private var reducers = mutableListOf<Reducer<State, Result>>()
    private var commandInterceptors = mutableListOf<Interceptor<Command>>()
    private var resultInterceptors = mutableListOf<Interceptor<Result>>()
    private var stateInterceptors = mutableListOf<Interceptor<State>>()
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
                transformers.addAll(it.transformers)
                commandInterceptors.addAll(it.interceptors)
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
                reducers.addAll(it.reducers)
                resultInterceptors.addAll(it.interceptors)
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
                stateInterceptors.addAll(it.interceptors)
            }
    }

    fun attach(plugin: StorePlugin<State, Command, Result>) {
        plugin.run {
            initialState = changeInitialState(initialState)
            transformers = changeTransformers(transformers).toMutableList()
            reducers = changeReducers(reducers).toMutableList()
            commandInterceptors = changeCommandInterceptors(commandInterceptors).toMutableList()
            resultInterceptors = changeResultInterceptors(resultInterceptors).toMutableList()
            stateInterceptors = changeStateInterceptors(stateInterceptors).toMutableList()
            observeScheduler = changeObserveScheduler(observeScheduler)
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
