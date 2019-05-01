package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.Test

class IfObjectInstanceOfTest {

    @Test
    fun `null is not consumed`() {
        var consumed = false
        (null as String?).mapIfInstanceOf(Any::class.java) {
            consumed = true
        }
        assert(consumed).isFalse()
    }

    @Test
    fun `non matching type is not consumed`() {
        var consumed = false
        "test".mapIfInstanceOf(List::class.java) {
            consumed = true
        }
        assert(consumed).isFalse()
    }

    @Test
    fun `matching type is consumed`() {
        var consumed = false
        "test".mapIfInstanceOf(String::class.java) {
            consumed = true
        }
        assert(consumed).isTrue()
    }

    @Test
    fun `matching subtype is consumed`() {
        var consumed = false
        "test".mapIfInstanceOf(CharSequence::class.java) {
            consumed = true
        }
        assert(consumed).isTrue()
    }

    @Test
    fun `matching type returns value`() {
        val result = "test".mapIfInstanceOf(String::class.java) {
            Unit
        }
        assert(result).isEqualTo(Unit)
    }

    @Test
    fun `non matching type returns null`() {
        val result = "test".mapIfInstanceOf(List::class.java) {
            Unit
        }
        assert(result).isNull()
    }
}
