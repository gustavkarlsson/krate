package se.gustavkarlsson.krate.vcr.implementations

import io.reactivex.Flowable
import se.gustavkarlsson.krate.vcr.Sample
import se.gustavkarlsson.krate.vcr.Tape
import se.gustavkarlsson.krate.vcr.Vcr

class InMemoryVcr<State : Any> : Vcr<State, String>() {

    private val shelf = hashMapOf<String, Tape<State>>()

    override fun newTape(tapeId: String): Tape<State> = InMemoryTape<State>().also { shelf[tapeId] = it }

    override fun loadTape(tapeId: String): Tape<State> =
        shelf[tapeId] ?: throw IllegalArgumentException("Tape not found: $tapeId")

    private class InMemoryTape<State : Any> : Tape<State> {
        private val samples = mutableListOf<Sample<State>>()

        override fun append(sample: Sample<State>) = samples.plusAssign(sample)

        override fun stop() = Unit

        override fun play(): Flowable<Sample<State>> = Flowable.fromIterable(samples.toList())
    }
}
