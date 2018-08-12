package se.gustavkarlsson.krate.vcr

import io.reactivex.Flowable

interface Tape<State : Any> {
    fun record(sample: Sample<State>)
    fun stop()
    fun play(): Flowable<Sample<State>>
}
