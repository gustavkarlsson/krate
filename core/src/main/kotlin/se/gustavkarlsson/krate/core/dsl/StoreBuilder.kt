package se.gustavkarlsson.krate.core.dsl

import Reducer
import Transformer
import Watcher
import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Store

class StoreBuilder<State : Any, Command : Any, Result : Any>
internal constructor() {
    private var initialState: State? = null
    private val transformers = mutableListOf<Transformer<State, Command, Result>>()
    private val reducers = mutableListOf<Reducer<State, Result>>()
    private val commandWatchers = mutableListOf<Watcher<Command>>()
    private val resultWatchers = mutableListOf<Watcher<Result>>()
    private val stateWatchers = mutableListOf<Watcher<State>>()
    private val errorWatchers = mutableListOf<Watcher<Throwable>>()
    private var observeScheduler: Scheduler? = null
    private var retryOnError: Boolean = false

    fun commands(block: Commands<State, Command, Result>.() -> Unit) {
        Commands<State, Command, Result>()
            .also(block)
            .let {
                transformers += it.transformers
                commandWatchers += it.watchers
            }
    }

    fun results(block: Results<State, Result>.() -> Unit) {
        Results<State, Result>()
            .also(block)
            .let {
                reducers += it.reducers
                resultWatchers += it.watchers
            }
    }

    fun states(block: States<State>.() -> Unit) {
        States(initialState, observeScheduler)
            .also(block)
            .let {
                initialState = it.initial
                observeScheduler = it.observeScheduler
                stateWatchers += it.watchers
            }
    }

    fun errors(block: Errors.() -> Unit) {
        Errors(retryOnError)
            .also(block)
            .let {
                retryOnError = it.retry
                errorWatchers += it.watchers
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
            commandWatchers,
            resultWatchers,
            stateWatchers,
            errorWatchers,
            observeScheduler,
            retryOnError
        )
    }
}
