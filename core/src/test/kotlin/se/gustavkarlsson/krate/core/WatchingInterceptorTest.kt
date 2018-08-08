package se.gustavkarlsson.krate.core

import Watcher
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

class WatchingInterceptorTest {

    private val mockWatcher = mock<Watcher<Int>> {
        on(it.invoke(anyInt())).thenReturn(Unit)
    }

    private val impl = WatchingInterceptor(mockWatcher)

    @Test
    fun `watcher runs`() {
        val observable = Observable.just(5)

        impl(observable).subscribe()

        verify(mockWatcher).invoke(5)
    }

    @Test
    fun `returned observable is unchanged`() {
        val observable = Observable.just(5)

        val observer = impl(observable).test()

        observer.assertValue(5)
        observer.assertComplete()
    }
}
