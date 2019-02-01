package se.gustavkarlsson.krate.vcr.implementations

import io.reactivex.Flowable
import se.gustavkarlsson.krate.vcr.Sample
import se.gustavkarlsson.krate.vcr.Tape
import se.gustavkarlsson.krate.vcr.Vcr

class InMemoryVcr<State : Any> : Vcr<State>() {

    private val shelf = hashMapOf<String, Tape<State>>()

    override fun newTape(name: String): Tape<State> = InMemoryTape<State>().also { shelf[name] = it }

    override fun loadTape(name: String): Tape<State> =
        shelf[name] ?: throw IllegalArgumentException("Tape not found: $name")

    private class InMemoryTape<State : Any> : Tape<State> {
        private val samples = mutableListOf<Sample<State>>()

        override fun append(sample: Sample<State>) = samples.plusAssign(sample)

        override fun stop() = Unit

        override fun play(): Flowable<Sample<State>> = Flowable.fromIterable(samples.toList())
    }
}
