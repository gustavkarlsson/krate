package se.gustavkarlsson.krate.core

import io.reactivex.Observable
import org.junit.Assert.fail
import org.junit.Test

class StateIgnoringTransformerTest {

    @Test
    fun `transforms elements correctly`() {
        val impl = StateIgnoringTransformer<Int, String> { it.map(Int::toString) }

        val results = impl.invoke(Observable.just(5)) { Unit }

        val observer = results.test()
        observer.assertValues("5")
    }

    @Test
    fun `does not run getState`() {
        val impl = StateIgnoringTransformer<Int, String> { it.map(Int::toString) }

        val results = impl.invoke(Observable.just(5)) { fail("getState was run but should not have") }

        val observer = results.test()
        observer.assertValueCount(1)
        observer.assertNoErrors()
    }
}
