package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

fun <T> Flowable<T>.blockingList(): List<T> = blockingIterable().toList()
