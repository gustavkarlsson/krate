import io.reactivex.Observable

internal typealias Transformer<State, Command, Result> = (Observable<Command>, () -> State) -> Observable<Result>

internal typealias Reducer<State, Result> = (State, Result) -> State

internal typealias Watcher<Type> = (Type) -> Unit
