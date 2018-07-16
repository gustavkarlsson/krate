package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class IsObjectInstanceOfTest {

    @Test
    fun `null is not consumed`() {
        var consumed = false
        (null as String?).ifObjectInstanceOf(Any::class) {
            consumed = true
        }
        assert(consumed).isFalse()
    }

    @Test
    fun `non matching type is not consumed`() {
        var consumed = false
        "test".ifObjectInstanceOf(List::class) {
            consumed = true
        }
        assert(consumed).isFalse()
    }

    @Test
    fun `matching type is consumed`() {
        var consumed = false
        "test".ifObjectInstanceOf(String::class) {
            consumed = true
        }
        assert(consumed).isTrue()
    }

    @Test
    fun `matching type returns value`() {
        val result = "test".ifObjectInstanceOf(String::class) {
            Unit
        }
        assert(result).isEqualTo(Unit)
    }

    @Test
    fun `non matching type returns null`() {
        val result = "test".ifObjectInstanceOf(List::class) {
            Unit
        }
        assert(result).isNull()
    }
}
