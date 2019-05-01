package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.reactivex.Flowable
import org.junit.Test

class TypedInterceptorTest {

    private val impl = TypedInterceptor<Number, Long, Int>(Int::class.javaObjectType) { commands ->
        commands.map { -it.toLong() }
    }

    @Test
    fun `non matching type will be ignored`() {
        val result = impl(Flowable.just(2.0)).blockingList()

        assert(result).isEmpty()
    }

    @Test
    fun `matching type will be consumed`() {
        val result = impl(Flowable.just(2)).blockingList()

        assert(result).containsExactly(-2L)
    }
}
