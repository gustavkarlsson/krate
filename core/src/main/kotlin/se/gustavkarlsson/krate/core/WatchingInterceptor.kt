package se.gustavkarlsson.krate.core

import Interceptor
import Watcher
import io.reactivex.Observable

internal class WatchingInterceptor<T>(private val watcher: Watcher<T>) : Interceptor<T> {
    override fun invoke(observable: Observable<T>): Observable<T> = observable.doOnNext(watcher)
}
