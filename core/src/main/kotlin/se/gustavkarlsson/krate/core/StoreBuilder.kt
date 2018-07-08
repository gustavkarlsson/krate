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

    fun setInitialState(state: State) {
        initialState = state
    }

    fun <C : Command> addTransformer(type: Class<C>, transformer: Transformer<State, C, Result>) {
        transformers += { commands: Observable<Command>, getState ->
            transformer(commands.ofType(type), getState)
        }
    }

    inline fun <reified C : Command> addTransformer(noinline transformer: Transformer<State, C, Result>) {
        addTransformer(C::class.java, transformer)
    }

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

    inline fun <reified R : Result> addReducer(noinline reduce: (State, R) -> State) {
        addReducer(R::class, reduce)
    }

    inline fun <reified C : Command> addCommandListener(crossinline block: (C) -> Unit) {
        addTransformer<C> { commands, _ ->
            commands.flatMap {
                block(it)
                Observable.empty<Result>()
            }
        }
    }

    inline fun <reified R : Result> addResultListener(crossinline block: (R) -> Unit) {
        addReducer { state: State, result: R ->
            block(result)
            state
        }
    }

    fun addStateListener(block: (State) -> Unit) {
        finalReducers += { state, _ ->
            block(state)
            state
        }
    }

    fun setObserveScheduler(scheduler: Scheduler?) {
        observeScheduler = scheduler
    }

    fun build(): Store<State, Command, Result> {
        val initialState = initialState
            ?: throw IllegalStateException("No initial state set")
        if (transformers.isEmpty()) {
            throw IllegalStateException("No command transformers defined")
        }
        if (reducers.isEmpty()) throw IllegalStateException("No result reducers defined")
        return Store(
            initialState,
            transformers,
            reducers + finalReducers,
            observeScheduler
        )
    }
}
