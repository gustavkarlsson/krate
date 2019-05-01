package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer

interface StorePlugin<State : Any, Command : Any> {
    fun changeInitialState(initialState: State?): State? = initialState

    fun changeReducers(
        reducers: List<Reducer<State, Command>>,
        getState: () -> State
    ): List<Reducer<State, Command>> = reducers

    fun changeCommandInterceptors(
        interceptors: List<Interceptor<Command>>,
        getState: () -> State
    ): List<Interceptor<Command>> = interceptors

    fun changeStateInterceptors(
        interceptors: List<Interceptor<State>>,
        getState: () -> State
    ): List<Interceptor<State>> = interceptors

    fun changeObserveScheduler(scheduler: Scheduler?): Scheduler? = scheduler
}
