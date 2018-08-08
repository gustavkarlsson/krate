package se.gustavkarlsson.krate.core

import Interceptor
import Watcher
import io.reactivex.Flowable

internal class WatchingInterceptor<T>(private val watcher: Watcher<T>) : Interceptor<T> {
    override fun invoke(stream: Flowable<T>): Flowable<T> = stream.doOnNext(watcher)
}
