package se.gustavkarlsson.krate.core

import Transformer
import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CompositeTransformerTest {

    private val mockTransformer1 = mock<Transformer<Long, Boolean, Int>> {
        on(it.invoke(any(), any())).thenAnswer {
            (it.arguments[0] as Observable<*>)
                .map { 1 }
        }
    }
    private val mockTransformer2 = mock<Transformer<Long, Boolean, Int>> {
        on(it.invoke(any(), any())).thenAnswer {
            (it.arguments[0] as Observable<*>)
                .flatMap { Observable.just(2, 3) }
        }
    }

    private val impl = CompositeTransformer(listOf(mockTransformer1, mockTransformer2)) { 0 }

    @Test
    fun `no transformers results in empty stream`() {
        val impl = CompositeTransformer<Long, Boolean, Int>(emptyList()) { 0 }

        val results = impl.invoke(Observable.just(true))

        val observer = results.test()
        observer.assertValueCount(0)
        observer.assertComplete()
    }

    @Test
    fun `transformers are run in order`() {
        impl.invoke(Observable.just(true))
            .subscribe()

        inOrder(mockTransformer1, mockTransformer2) {
            verify(mockTransformer1).invoke(any(), any())
            verify(mockTransformer2).invoke(any(), any())
        }
    }

    @Test
    fun `transformer results are merged`() {
        val results = impl.invoke(Observable.just(true))

        val observer = results.test()
        observer.assertValueSet(setOf(1, 2, 3))
    }

    @Test
    fun `source observable is subscribed only once with multiple transformers`() {
        var subscriptions = 0
        val source = Observable.empty<Boolean>()
            .doOnSubscribe { subscriptions++ }

        impl.invoke(source)
            .subscribe()

        assert(subscriptions, "subscriptions").isEqualTo(1)
    }
}
