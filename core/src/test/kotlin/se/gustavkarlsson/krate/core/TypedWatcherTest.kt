package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class TypedWatcherTest {

    private var consumed = false
    private val impl = TypedWatcher<Number, Int>(Int::class) {
        consumed = true
    }

    @Test
    fun `non matching type will be ignored`() {
        impl(2.0)

        assert(consumed).isFalse()
    }

    @Test
    fun `matching type will be consumed`() {
        impl(2)

        assert(consumed).isTrue()
    }
}
