package se.gustavkarlsson.krate.core.dsl

import io.reactivex.Scheduler
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateAwareTransformer

interface StorePlugin<State : Any, Command : Any, Result : Any> {
    fun changeInitialState(initialState: State?): State? = initialState

    fun changeTransformers(
        transformers: List<StateAwareTransformer<State, Command, Result>>
    ): List<StateAwareTransformer<State, Command, Result>> = transformers

    fun changeReducer(reducer: Reducer<State, Result>?): Reducer<State, Result>? = reducer

    fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> = interceptors

    fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> = interceptors

    fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> = interceptors

    fun changeObserveScheduler(scheduler: Scheduler?): Scheduler? = scheduler
}
