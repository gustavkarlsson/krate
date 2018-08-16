package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Flowable
import org.junit.Test

class CompositeTransformerTest {

    private val mockTransformer1 = mock<StateAwareTransformer<Long, Boolean, Int>> {
        on(it.invoke(any(), any())).thenAnswer {
            (it.arguments[0] as Flowable<*>)
                .map { 1 }
        }
    }
    private val mockTransformer2 = mock<StateAwareTransformer<Long, Boolean, Int>> {
        on(it.invoke(any(), any())).thenAnswer {
            (it.arguments[0] as Flowable<*>)
                .flatMap { Flowable.just(2, 3) }
        }
    }

    private val impl = CompositeTransformer(listOf(mockTransformer1, mockTransformer2)) { 0 }

    @Test
    fun `no transformers results in empty stream`() {
        val impl = CompositeTransformer<Long, Boolean, Int>(emptyList()) { 0 }

        val results = impl.invoke(Flowable.just(true))

        val observer = results.test()
        observer.assertValueCount(0)
        observer.assertComplete()
    }

    @Test
    fun `transformers are run in order`() {
        impl.invoke(Flowable.just(true))
            .subscribe()

        inOrder(mockTransformer1, mockTransformer2) {
            verify(mockTransformer1).invoke(any(), any())
            verify(mockTransformer2).invoke(any(), any())
        }
    }

    @Test
    fun `transformer results are merged`() {
        val results = impl.invoke(Flowable.just(true))

        val observer = results.test()
        observer.assertValueSet(setOf(1, 2, 3))
    }

    @Test
    fun `source stream is subscribed only once with multiple transformers`() {
        var subscriptions = 0
        val source = Flowable.empty<Boolean>()
            .doOnSubscribe { subscriptions++ }

        impl.invoke(source)
            .subscribe()

        assert(subscriptions, "subscriptions").isEqualTo(1)
    }
}
