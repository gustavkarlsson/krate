package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Store

/**
 * A configuration block for a [Store].
 */
@StoreDsl
class StoreBuilder<State : Any, Command : Any>
internal constructor(private val stateDelegate: StateDelegate<State>) {
    private var initialState: State?
        get() = stateDelegate.value
        set(value) {
            stateDelegate.value = value
        }
    private var reducers = mutableListOf<Reducer<State, Command>>()
    private var commandInterceptors = mutableListOf<Interceptor<Command>>()
    private var stateInterceptors = mutableListOf<Interceptor<State>>()
    private var observeScheduler: Scheduler? = null

    /**
     * Configures commands for the store.
     *
     * At least one transformer should be defined.
     *
     * @param block the code used to configure commands
     */
    fun commands(block: Commands<Command>.() -> Unit) {
        Commands<Command>()
            .also(block)
            .also {
                commandInterceptors.addAll(it.interceptors)
            }
    }

    /**
     * Configures states for the store.
     *
     * [States.initial] must be set in at least one states block.
     *
     * @param block the code used to configure states
     */
    fun states(block: States<State, Command>.() -> Unit) {
        States<State, Command>(stateDelegate, observeScheduler)
            .also(block)
            .also {
                initialState = it.initial
                reducers.addAll(it.reducers)
                stateInterceptors.addAll(it.interceptors)
                observeScheduler = it.observeScheduler
            }
    }

    /**
     * Adds a plugin to the store
     *
     * @param plugin the plugin to add
     */
    fun plugin(plugin: StorePlugin<State, Command>) {
        plugin.run {
            initialState = changeInitialState(initialState)
            reducers = changeReducers(reducers, stateDelegate::valueUnsafe).toMutableList()
            commandInterceptors =
                changeCommandInterceptors(commandInterceptors, stateDelegate::valueUnsafe).toMutableList()
            stateInterceptors = changeStateInterceptors(stateInterceptors, stateDelegate::valueUnsafe).toMutableList()
            observeScheduler = changeObserveScheduler(observeScheduler)
        }
    }

    internal fun build(): Store<State, Command> {
        checkNotNull(stateDelegate.value) {
            "No initial state set. Set the initial state in a states-block in the DSL"
        }
        return Store(
            stateDelegate,
            reducers,
            commandInterceptors,
            stateInterceptors,
            observeScheduler
        )
    }
}
