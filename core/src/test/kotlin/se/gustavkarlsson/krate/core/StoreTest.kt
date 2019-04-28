package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class StoreTest {
    private val text = "content"

    private val initialState = NotesState()

    private val transformer2Result = NoteCreated(Note(text))

    private val newState = NotesState(listOf(Note(text)))

    private val mockTransformer1 = mock<Transformer< NotesCommand, NotesResult>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            val commands = invocation.arguments[0] as Flowable<*>
            commands.flatMap { Flowable.empty<NotesResult>() }
        }
    }

    private val mockTransformer2 = mock<Transformer< NotesCommand, NotesResult>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            val commands = invocation.arguments[0] as Flowable<*>
            commands
                .ofType(CreateNote::class.java)
                .map { transformer2Result }
        }
    }

    private val mockReducer = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer { invocation ->
            val state = invocation.arguments[0] as NotesState
            val result = invocation.arguments[1] as NoteCreated
            NotesState(state.notes + result.note)
        }
    }

    private val mockCommandInterceptor1 = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val mockCommandInterceptor2 = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val mockResultInterceptor1 = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val mockResultInterceptor2 = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val mockStateInterceptor1 = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val mockStateInterceptor2 = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }

    private val testScheduler = TestScheduler()

    private val impl = Store(
        StateDelegate(initialState),
        listOf(mockTransformer1, mockTransformer2),
        mockReducer,
        listOf(mockCommandInterceptor1, mockCommandInterceptor2),
        listOf(mockResultInterceptor1, mockResultInterceptor2),
        listOf(mockStateInterceptor1, mockStateInterceptor2),
        testScheduler
    )

    @Test
    fun `currentState before any command has initialState`() {
        val result = impl.currentState

        assert(result).isEqualTo(initialState)
    }

    @Test
    fun `currentState after processing chain has latest state`() {
        impl.issue(CreateNote(text))

        val result = impl.currentState

        assert(result).isEqualTo(newState)
    }

    @Test
    fun `subscribe before any command gets initial state`() {
        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(initialState)
    }

    @Test
    fun `second subscribe before any command also gets initial state`() {
        impl.states.subscribe()
        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(initialState)
    }

    @Test
    fun `subscribe after processing chain gets latest state`() {
        impl.issue(CreateNote(text))

        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(newState)
    }

    @Test
    fun `subscribe twice runs reducer only once`() {
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(text))
        testScheduler.triggerActions()

        verify(mockReducer).invoke(any(), any())
    }

    @Test
    fun `command interceptors are called in order`() {
        val note = CreateNote(text)

        impl.issue(note)

        inOrder(mockCommandInterceptor1, mockCommandInterceptor2) {
            verify(mockCommandInterceptor1).invoke(any())
            verify(mockCommandInterceptor2).invoke(any())
        }
    }

    @Test
    fun `transformers are called in order`() {
        inOrder(mockTransformer1, mockTransformer2) {
            verify(mockTransformer1).invoke(any())
            verify(mockTransformer2).invoke(any())
        }
    }

    @Test
    fun `result interceptors are called in order`() {
        impl.issue(CreateNote(text))

        inOrder(mockResultInterceptor1, mockResultInterceptor2) {
            verify(mockResultInterceptor1).invoke(any())
            verify(mockResultInterceptor2).invoke(any())
        }
    }

    @Test
    fun `state interceptors are called in order`() {
        impl.issue(CreateNote(text))

        inOrder(mockStateInterceptor1, mockStateInterceptor2) {
            verify(mockStateInterceptor1).invoke(any())
            verify(mockStateInterceptor2).invoke(any())
        }
    }

    @Test
    fun `isDisposed is initially false`() {
        val result = impl.isDisposed

        assert(result).isFalse()
    }

    @Test
    fun `isDisposed is true after disposing`() {
        impl.dispose()

        val result = impl.isDisposed

        assert(result).isTrue()
    }

    @Test
    fun `dispose() can be called twice`() {
        impl.dispose()
        impl.dispose()
    }

    @Test
    fun `commands issued after dispose() are ignored, even if subscribed`() {
        impl.states.subscribe()
        impl.dispose()

        impl.issue(CreateNote(text))
        val result = impl.currentState

        assert(result).isEqualTo(initialState)
    }
}
