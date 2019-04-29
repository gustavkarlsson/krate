package se.gustavkarlsson.krate.core

import io.reactivex.Scheduler

fun <State : Any, Command : Any, Result : Any> mergeStores(
    storeA: Store<State, Command, Result>,
    storeB: Store<State, Command, Result>,
    initialState: State = storeA.currentState,
    observeScheduler: Scheduler? = storeA.observeScheduler
): Store<State, Command, Result> =
    Store(
        StateDelegate(initialState),
        storeA.transformers + storeB.transformers,
        storeA.reducers + storeB.reducers,
        storeA.commandInterceptors + storeB.commandInterceptors,
        storeA.resultInterceptors + storeB.resultInterceptors,
        storeA.stateInterceptors + storeB.stateInterceptors,
        observeScheduler = observeScheduler
    )
