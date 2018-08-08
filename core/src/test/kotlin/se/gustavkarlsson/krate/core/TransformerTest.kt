package se.gustavkarlsson.krate.core

import io.reactivex.Flowable
import org.junit.Assert.fail
import org.junit.Test

class TransformerTest {

    @Test
    fun `transforms elements correctly`() {
        val impl = Transformer<Int, String> { it.map(Int::toString) }

        val results = impl.invoke(Flowable.just(5)) { Unit }

        val observer = results.test()
        observer.assertValues("5")
    }

    @Test
    fun `does not run getState`() {
        val impl = Transformer<Int, String> { it.map(Int::toString) }

        val results = impl.invoke(Flowable.just(5)) { fail("getState was run but should not have") }

        val observer = results.test()
        observer.assertValueCount(1)
        observer.assertNoErrors()
    }
}
