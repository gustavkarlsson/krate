import io.reactivex.Observable

typealias StateIgnoringTransformer<Command, Result> = (Observable<Command>) -> Observable<Result>

typealias StateAwareTransformer<State, Command, Result> = (Observable<Command>, () -> State) -> Observable<Result>

typealias Reducer<State, Result> = (State, Result) -> State

typealias Interceptor<Type> = (Observable<Type>) -> Observable<Type>

typealias Watcher<Type> = (Type) -> Unit
