package se.gustavkarlsson.krate.vcr.implementations

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import se.gustavkarlsson.krate.vcr.Recording
import se.gustavkarlsson.krate.vcr.Sample
import se.gustavkarlsson.krate.vcr.Vcr

class InMemoryVcr<State : Any> : Vcr<State, String>() {

    private val shelf = hashMapOf<String, List<Sample<State>>>()

    override fun startRecording(tapeId: String): Single<Recording<State>> {
        val samples = mutableListOf<Sample<State>>()
        shelf[tapeId] = samples
        return Single.just(InMemoryRecording(samples))
    }


    override fun startPlaying(tapeId: String): Flowable<Sample<State>> {
        val samples = shelf[tapeId] ?: return Flowable.error(IllegalArgumentException("Tape not found: $tapeId"))
        return Flowable.fromIterable(samples)
    }

    override fun eraseTape(tapeId: String): Completable {
        shelf -= tapeId
        return Completable.complete()
    }

    private class InMemoryRecording<State : Any>(private val samples: MutableList<Sample<State>>) : Recording<State> {
        override fun write(sample: Sample<State>): Completable {
            samples.plusAssign(sample)
            return Completable.complete()
        }

        override fun isDisposed(): Boolean = false

        override fun dispose() = Unit

    }
}
