package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.containsExactly
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

class WatchingInterceptorTest {

    private val mockWatcher = mock<Watcher<Int>> {
        on(it.invoke(anyInt())).thenReturn(Unit)
    }

    private val impl = WatchingInterceptor(mockWatcher)

    @Test
    fun `watcher runs`() {
        val stream = Flowable.just(5)

        impl(stream).subscribe()

        verify(mockWatcher).invoke(5)
    }

    @Test
    fun `returned stream is unchanged`() {
        val stream = Flowable.just(5)

        val results = impl(stream).blockingList()

        assert(results).containsExactly(5)
    }
}
