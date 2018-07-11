package se.gustavkarlsson.krate.core

import io.reactivex.Observable
import io.reactivex.Scheduler
import kotlin.reflect.KClass

class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor() {
    private var initialState: State? = null
    private val transformers =
        mutableListOf<Transformer<State, Command, Result>>()
    private val reducers =
        mutableListOf<Reducer<State, Result>>()
    private val finalReducers =
        mutableListOf<Reducer<State, Result>>()
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
     * Adds a transformer to the store. A transformer converts commands of type [C] to results.
     *
     * @param type the type of commands to transform
     * @param transformer the transformer
     */
    fun <C : Command> addTransformer(type: Class<C>, transformer: Transformer<State, C, Result>) {
        transformers += { commands: Observable<Command>, getState ->
            transformer(commands.ofType(type), getState)
        }
    }

    /**
     * Adds a transformer to the store. A transformer converts commands of type [C] to results.
     *
     * @param transformer the transformer
     */
    inline fun <reified C : Command> addTransformer(noinline transformer: Transformer<State, C, Result>) {
        addTransformer(C::class.java, transformer)
    }

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a result of type [R] to produce a new state.
     *
     * @param type the type of results to use for creating new states
     * @param reduce the reducer
     */
    fun <R : Result> addReducer(type: KClass<R>, reduce: (State, R) -> State) {
        reducers += { currentState, result ->
            if (type.java.isInstance(result)) {
                @Suppress("UNCHECKED_CAST")
                reduce(currentState, result as R)
            } else {
                currentState
            }
        }
    }

    /**
     * Adds a reducer to the store.
     *
     * A reducer takes the current state of the store and a result of type [R] to produce a new state.
     *
     * @param reduce the reducer
     */
    inline fun <reified R : Result> addReducer(noinline reduce: (State, R) -> State) {
        addReducer(R::class, reduce)
    }

    /**
     * Adds a listener that gets called for every issued command of type [C]
     */
    inline fun <reified C : Command> addCommandListener(crossinline block: (C) -> Unit) {
        addTransformer<C> { commands, _ ->
            commands.flatMap {
                block(it)
                Observable.empty<Result>()
            }
        }
    }

    /**
     * Adds a listener that gets called for every produced result type [R]
     */
    inline fun <reified R : Result> addResultListener(crossinline block: (R) -> Unit) {
        addReducer { state: State, result: R ->
            block(result)
            state
        }
    }

    /**
     * Adds a listener that gets called for every state change
     */
    fun addStateListener(block: (State) -> Unit) {
        finalReducers += { state, _ ->
            block(state)
            state
        }
    }

    /**
     * Sets a scheduler that will be used to observe state changes.
     *
     * @param scheduler the scheduler, or null if no specific scheduler should be used
     */
    fun setObserveScheduler(scheduler: Scheduler?) {
        observeScheduler = scheduler
    }

    internal fun build(): Store<State, Command, Result> {
        val initialState = initialState ?: throw IllegalStateException("No initial state set")
        if (transformers.isEmpty()) throw IllegalStateException("No transformers defined")
        if (reducers.isEmpty()) throw IllegalStateException("No reducers defined")
        return Store(
            initialState,
            transformers,
            reducers + finalReducers,
            observeScheduler
        )
    }
}
