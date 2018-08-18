package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class StoreTest {
    private val text = "content"

    private val initialState = NotesState()

    private val transformer2Result = NoteCreated(Note(text))

    private val newState = NotesState(listOf(Note(text), Note(text)))

    private val mockTransformer1 = mock<StateAwareTransformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Flowable<*>
            commands
                .flatMap { Flowable.empty<NotesResult>() }
        }
    }

    private val mockTransformer2 = mock<StateAwareTransformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Flowable<*>
            commands
                .ofType(CreateNote::class.java)
                .map { transformer2Result }
        }
    }

    private val mockReducer1 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val state = it.arguments[0] as NotesState
            val result = it.arguments[1] as NoteCreated
            NotesState(state.notes + result.note)
        }
    }

    private val mockReducer2 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val state = it.arguments[0] as NotesState
            val result = it.arguments[1] as NoteCreated
            NotesState(state.notes + result.note)
        }
    }

    private val mockCommandInterceptor1 = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val mockCommandInterceptor2 = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val mockResultInterceptor1 = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val mockResultInterceptor2 = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val mockStateInterceptor1 = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val mockStateInterceptor2 = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }

    private val testScheduler = TestScheduler()

    private val impl = Store(
        initialState,
        listOf(mockTransformer1, mockTransformer2),
        listOf(mockReducer1, mockReducer2),
        listOf(mockCommandInterceptor1, mockCommandInterceptor2),
        listOf(mockResultInterceptor1, mockResultInterceptor2),
        listOf(mockStateInterceptor1, mockStateInterceptor2),
        testScheduler
    )

    @Before
    fun setUp() {
        impl.subscribeInternal()
    }

    @Test
    fun `currentState before any command has initialState`() {
        val result = impl.currentState

        assert(result).isEqualTo(initialState)
    }

    @Test
    fun `currentState after processing chain has latest state`() {
        impl.subscribeInternal()
        impl.issue(CreateNote(text))

        val result = impl.currentState

        assert(result).isEqualTo(newState)
    }

    @Test
    fun `subscribe before any command gets initial state`() {
        impl.subscribeInternal()

        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(initialState)
    }

    @Test
    fun `second subscribe before any command also gets initial state`() {
        impl.subscribeInternal()

        impl.states.subscribe()
        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(initialState)
    }

    @Test
    fun `subscribe after processing chain gets latest state`() {
        impl.subscribeInternal()
        impl.issue(CreateNote(text))

        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(newState)
    }

    @Test
    fun `command interceptors are called in order`() {
        impl.subscribeInternal()
        val note = CreateNote(text)

        impl.issue(note)

        inOrder(mockCommandInterceptor1, mockCommandInterceptor2) {
            verify(mockCommandInterceptor1).invoke(any())
            verify(mockCommandInterceptor2).invoke(any())
        }
    }

    @Test
    fun `transformers are called in order`() {
        impl.subscribeInternal()

        inOrder(mockTransformer1, mockTransformer2) {
            verify(mockTransformer1).invoke(any(), any())
            verify(mockTransformer2).invoke(any(), any())
        }
    }

    @Test
    fun `result interceptors are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(text))

        inOrder(mockResultInterceptor1, mockResultInterceptor2) {
            verify(mockResultInterceptor1).invoke(any())
            verify(mockResultInterceptor2).invoke(any())
        }
    }

    @Test
    fun `reducers are called in order`() {
        impl.subscribeInternal()
        val text = text

        impl.issue(CreateNote(text))

        inOrder(mockReducer1, mockReducer2) {
            verify(mockReducer1).invoke(initialState, transformer2Result)
            verify(mockReducer2).invoke(NotesState(initialState.notes + Note(text)), transformer2Result)
        }
    }

    @Test
    fun `state interceptors are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(text))

        inOrder(mockStateInterceptor1, mockStateInterceptor2) {
            verify(mockStateInterceptor1).invoke(any())
            verify(mockStateInterceptor2).invoke(any())
        }
    }
}
