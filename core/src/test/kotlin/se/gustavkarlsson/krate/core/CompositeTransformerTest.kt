package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Flowable
import org.junit.Test

class CompositeTransformerTest {

    private val mockTransformer1 = mock<Transformer<Boolean, Int>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            val commands = invocation.arguments[0] as Flowable<*>
            commands.map { 1 }
        }
    }
    private val mockTransformer2 = mock<Transformer<Boolean, Int>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            val commands = invocation.arguments[0] as Flowable<*>
            commands.flatMap { Flowable.just(2, 3) }
        }
    }

    private val impl = CompositeTransformer(listOf(mockTransformer1, mockTransformer2))

    @Test
    fun `no transformers results in empty stream`() {
        val impl = CompositeTransformer<Boolean, Int>(emptyList())

        val results = impl.invoke(Flowable.just(true)).blockingList()

        assert(results).isEmpty()
    }

    @Test
    fun `transformers are run in order`() {
        impl.invoke(Flowable.just(true))
            .subscribe()

        inOrder(mockTransformer1, mockTransformer2) {
            verify(mockTransformer1).invoke(any())
            verify(mockTransformer2).invoke(any())
        }
    }

    @Test
    fun `transformer results are merged`() {
        val results = impl.invoke(Flowable.just(true)).blockingList()

        assert(results).containsExactly(1, 2, 3)
    }

    @Test
    fun `source stream is subscribed only once with multiple transformers`() {
        var subscriptionCount = 0
        val source = Flowable.empty<Boolean>()
            .doOnSubscribe { subscriptionCount++ }

        impl.invoke(source).subscribe()

        assert(subscriptionCount, "subscriptionCount").isEqualTo(1)
    }
}
