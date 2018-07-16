package se.gustavkarlsson.krate.core

import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CompositeTransformerTest {

    @Test
    fun `no transformers results in empty stream`() {
        val transformer = CompositeTransformer<Int, Unit, Unit>(emptyList()) { 0 }

        val results = transformer.invoke(Observable.just(Unit))

        val observer = results.test()
        observer.assertValueCount(0)
        observer.assertComplete()
    }
}
