package se.gustavkarlsson.krate.core

import Reducer
import Transformer
import Watcher
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.ofType

class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor() {
    private var initialState: State? = null
    private val transformers = mutableListOf<Transformer<State, Command, Result>>()
    private val reducers = mutableListOf<Reducer<State, Result>>()
    private val commandWatchers = mutableListOf<Watcher<Command>>()
    private val resultWatchers = mutableListOf<Watcher<Result>>()
    private val stateWatchers = mutableListOf<Watcher<State>>()
    private var observeScheduler: Scheduler? = null

    /**
     * Sets the initial state of the store.
     *
     * @param state the state
     */
    fun setInitialState(state: State) {
        initialState = state
    }

    /**
     * Adds a transformer to the store.
     *
     * A transformer converts commands to results.
     *
     * @param transform the transformer function
     */
    fun transform(transform: Transformer<State, Command, Result>) {
        transformers += transform
    }

    /**
     * Adds a typed transformer to the store.
     *
     * A typed transformer converts commands of type [C] to results.
     *
     * @param C the type of commands to transform
     * @param transform the transformer function
     */
    inline fun <reified C : Command> transformByType(noinline transform: Transformer<State, C, Result>) {
        transform { getState ->
            ofType<C>().transform(getState)
        }
    }

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a result to produce a new state.
     *
     * @param reduce the reducer function
     */
    fun reduce(reduce: Reducer<State, Result>) {
        reducers += reduce
    }

    /**
     * Adds a typed reducer to the store.
     *
     * A typed reducer takes the current state of the store and a result of type [R] to produce a new state.
     *
     * @param reduce the reducer function
     */
    inline fun <reified R : Result> reduceByType(noinline reduce: (State, R) -> State) {
        reduce { currentState, result ->
            if (R::class.java.isInstance(result)) {
                @Suppress("UNCHECKED_CAST")
                reduce(currentState, result as R)
            } else {
                currentState
            }
        }
    }

    /**
     * Adds a command watcher to the store.
     *
     * A command watcher runs on each processed command
     *
     * @param watch the watcher function
     */
    fun watchCommands(watch: Watcher<Command>) {
        commandWatchers += watch
    }

    /**
     * Adds a typed command watcher to the store.
     *
     * A typed command watcher runs on each processed command of type [C]
     *
     * @param watch the watcher function
     */
    inline fun <reified C : Command> watchCommandsByType(noinline watch: Watcher<C>) {
        watchCommands { command ->
            if (C::class.java.isInstance(command)) {
                @Suppress("UNCHECKED_CAST")
                watch(command as C)
            }
        }
    }

    /**
     * Adds a result watcher to the store.
     *
     * A result watcher runs on each processed command
     *
     * @param watch the watcher function
     */
    fun watchResults(watch: Watcher<Result>) {
        resultWatchers += watch
    }

    /**
     * Adds a typed result watcher to the store.
     *
     * A typed result watcher runs on each processed result of type [R]
     *
     * @param watch the watcher function
     */
    inline fun <reified R : Result> watchResultsByType(noinline watch: Watcher<R>) {
        watchResults { result ->
            if (R::class.java.isInstance(result)) {
                @Suppress("UNCHECKED_CAST")
                watch(result as R)
            }
        }
    }

    /**
     * Adds a state watcher to the store.
     *
     * A state watcher runs on each processed state
     *
     * @param watch the watcher function
     */
    fun watchStates(watch: Watcher<State>) {
        stateWatchers += watch
    }

    /**
     * Adds a typed state watcher to the store.
     *
     * A typed state watcher runs on each processed state of type [S]
     *
     * @param watch the watcher function
     */
    inline fun <reified S : State> watchStatesByType(noinline watch: Watcher<S>) {
        watchStates { state ->
            if (S::class.java.isInstance(state)) {
                @Suppress("UNCHECKED_CAST")
                watch(state as S)
            }
        }
    }

    /**
     * Sets a scheduler that will be used to observe state changes.
     *
     * @param scheduler the scheduler, or null if no specific scheduler should be used
     */
    fun observeOn(scheduler: Scheduler?) {
        observeScheduler = scheduler
    }

    internal fun build(): Store<State, Command, Result> {
        val initialState = checkNotNull(initialState) { "No initial state set" }
        check(!transformers.isEmpty()) { "No transformers defined" }
        check(!reducers.isEmpty()) { "No reducers defined" }
        return Store(
            initialState,
            transformers,
            reducers,
            commandWatchers,
            resultWatchers,
            stateWatchers,
            observeScheduler
        )
    }
}
