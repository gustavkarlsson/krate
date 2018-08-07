import io.reactivex.Observable

internal typealias StateIgnoringTransformer<Command, Result> = (Observable<Command>) -> Observable<Result>

internal typealias StateAwareTransformer<State, Command, Result> =
        (Observable<Command>, () -> State) -> Observable<Result>

internal typealias Reducer<State, Result> = (State, Result) -> State

internal typealias Interceptor<Type> = (Observable<Type>) -> Observable<Type>
