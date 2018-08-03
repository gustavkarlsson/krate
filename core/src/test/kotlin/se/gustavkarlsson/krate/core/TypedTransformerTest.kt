package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.reactivex.Observable
import org.junit.Test

class TypedTransformerTest {

    private val impl = TypedTransformer<Boolean, Number, Long, Int>(Int::class) { commands, getState ->
        commands.map {
            val invert = getState()
            if (invert) {
                -it.toLong()
            } else {
                it.toLong()
            }
        }
    }

    @Test
    fun `non matching type will be ignored`() {
        val result = impl(Observable.just(2.0)) { false }.blockingIterable().toList()

        assert(result).isEmpty()
    }

    @Test
    fun `matching type will be consumed`() {
        val result = impl(Observable.just(2)) { false }.blockingIterable().toList()

        assert(result).containsExactly(2L)
    }

    @Test
    fun `getState is used`() {
        val result = impl(Observable.just(2)) { true }.blockingIterable().toList()

        assert(result).containsExactly(-2L)
    }
}
