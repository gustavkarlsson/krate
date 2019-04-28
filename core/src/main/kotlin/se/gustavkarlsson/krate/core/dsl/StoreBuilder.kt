package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Store
import se.gustavkarlsson.krate.core.Transformer

/**
 * A configuration block for a [Store].
 */
@StoreDsl
class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor(private val stateDelegate: StateDelegate<State>) {
    private var initialState: State?
        get() = stateDelegate.value
        set(value) {
            stateDelegate.value = value
        }
    private var transformers = mutableListOf<Transformer<Command, Result>>()
    private var reducer: Reducer<State, Result>? = null
    private var commandInterceptors = mutableListOf<Interceptor<Command>>()
    private var resultInterceptors = mutableListOf<Interceptor<Result>>()
    private var stateInterceptors = mutableListOf<Interceptor<State>>()
    private var observeScheduler: Scheduler? = null

    /**
     * Configures commands for the store.
     *
     * At least one transformer must be defined.
     *
     * @param block the code used to configure commands
     */
    fun commands(block: Commands<Command, Result>.() -> Unit) {
        Commands<Command, Result>()
            .also(block)
            .let {
                transformers.addAll(it.transformers)
                commandInterceptors.addAll(it.interceptors)
            }
    }

    /**
     * Configures results for the store.
     *
     * The reducer must be defined.
     *
     * @param block the code used to configure results
     */
    fun results(block: Results<State, Result>.() -> Unit) {
        Results<State, Result>()
            .also(block)
            .let {
                reducer = it.reducer
                resultInterceptors.addAll(it.interceptors)
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
        States(stateDelegate, observeScheduler)
            .also(block)
            .let {
                initialState = it.initial
                observeScheduler = it.observeScheduler
                stateInterceptors.addAll(it.interceptors)
            }
    }

    /**
     * Adds a plugin to the store
     *
     * @param plugin the plugin to add
     */
    fun plugin(plugin: StorePlugin<State, Command, Result>) {
        plugin.run {
            initialState = changeInitialState(initialState)
            transformers = changeTransformers(transformers, stateDelegate::valueUnsafe).toMutableList()
            reducer = changeReducer(reducer)
            commandInterceptors =
                changeCommandInterceptors(commandInterceptors, stateDelegate::valueUnsafe).toMutableList()
            resultInterceptors =
                changeResultInterceptors(resultInterceptors, stateDelegate::valueUnsafe).toMutableList()
            stateInterceptors = changeStateInterceptors(stateInterceptors).toMutableList()
            observeScheduler = changeObserveScheduler(observeScheduler)
        }
    }

    internal fun build(): Store<State, Command, Result> {
        checkNotNull(stateDelegate.value) {
            "No initial state set. Set the initial state in a states-block in the DSL"
        }
        val reducer = checkNotNull(reducer) {
            "No reducer set. Set the reducer in a result-block in the DSL"
        }
        return Store(
            stateDelegate,
            transformers,
            reducer,
            commandInterceptors,
            resultInterceptors,
            stateInterceptors,
            observeScheduler
        )
    }
}
