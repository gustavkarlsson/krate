package se.gustavkarlsson.krate.vcr.implementations

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import se.gustavkarlsson.krate.vcr.Playback
import se.gustavkarlsson.krate.vcr.Recording
import se.gustavkarlsson.krate.vcr.Sample
import se.gustavkarlsson.krate.vcr.Vcr

class InMemoryVcr<State : Any, Command : Any, Result : Any>(
    private val shelf: MutableMap<String, List<Sample<State>>> = hashMapOf()
) : Vcr<State, Command, Result, String>() {

    override fun startRecording(tapeId: String): Single<Recording<State>> {
        if (shelf[tapeId] != null) return Single.error(IllegalArgumentException("Tape already exists: $tapeId"))
        val samples = mutableListOf<Sample<State>>()
        shelf[tapeId] = samples
        return Single.just(InMemoryRecording(samples))
    }

    override fun startPlaying(tapeId: String): Single<Playback<State>> {
        val samples = shelf[tapeId] ?: return Single.error(IllegalArgumentException("Tape not found: $tapeId"))
        return Single.just(InMemoryPlayback(samples))
    }

    override fun eraseTape(tapeId: String): Completable {
        if (shelf[tapeId] == null) return Completable.error(IllegalArgumentException("Tape not found: $tapeId"))
        shelf -= tapeId
        return Completable.complete()
    }

    private class InMemoryRecording<State : Any>(samples: MutableList<Sample<State>>) : Recording<State> {
        override val input: Consumer<Sample<State>> = Consumer { samples.plusAssign(it) }

        override fun isDisposed(): Boolean = false

        override fun dispose() = Unit
    }

    private class InMemoryPlayback<State : Any>(samples: List<Sample<State>>) : Playback<State> {
        override val output: Iterable<Sample<State>> = samples

        override fun isDisposed(): Boolean = false

        override fun dispose() = Unit
    }
}
