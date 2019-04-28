package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test

class TypedReducerTest {

    private val impl = TypedReducer<Long, Number, Int>(Int::class.javaObjectType) { state, result ->
        state + result
    }

    @Test
    fun `non matching type will be ignored`() {
        val result = impl(0, 2.0)

        assert(result).isEqualTo(0L)
    }

    @Test
    fun `matching type will be consumed`() {
        val result = impl(0, 2)

        assert(result).isEqualTo(2L)
    }
}
