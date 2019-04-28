package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class StateDelegateTest {

    private val impl = StateDelegate<String>()

    private val testHolder = object {
        var testProperty by impl
    }

    @Test(expected = IllegalStateException::class)
    fun `valueUnsafe before setting throws exception`() {
        impl.valueUnsafe
    }

    @Test
    fun `value before setting is null`() {
        assert(impl.value).isNull()
    }

    @Test
    fun `value after setting is value`() {
        val value = "foo"
        impl.value = value

        assert(impl.value).isEqualTo(value)
    }

    @Test
    fun `getValue after setting is value`() {
        val value = "foo"
        testHolder.testProperty = value

        assert(testHolder.testProperty).isEqualTo(value)
    }

    @Test(expected = IllegalStateException::class)
    fun `getValue before setting throws exception`() {
        testHolder.testProperty
    }
}
