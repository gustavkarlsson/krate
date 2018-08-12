package se.gustavkarlsson.krate.core.dsl

import Interceptor
import Reducer
import StateAwareTransformer
import io.reactivex.Scheduler

interface StorePlugin<State : Any, Command : Any, Result : Any> {
    fun changeInitialState(initialState: State?): State? = initialState

    fun changeTransformers(
        transformers: List<StateAwareTransformer<State, Command, Result>>
    ): List<StateAwareTransformer<State, Command, Result>> = transformers

    fun changeReducers(reducers: List<Reducer<State, Result>>): List<Reducer<State, Result>> = reducers

    fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> = interceptors

    fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> = interceptors

    fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> = interceptors

    fun changeObserveScheduler(scheduler: Scheduler?): Scheduler? = scheduler
}
