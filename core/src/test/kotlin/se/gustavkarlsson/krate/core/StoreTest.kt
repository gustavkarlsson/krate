package se.gustavkarlsson.krate.core

import Reducer
import Transformer
import Watcher
import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class StoreTest {
    private val text = "content"

    private val initialState = NotesState()

    private val transformer2Result = NoteCreated(Note(text))

    private val error = Exception("error")

    private val newState = NotesState(listOf(Note(text), Note(text)))

    private val mockTransformer1 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands
                .flatMap { Observable.empty<NotesResult>() }
        }
    }

    private val mockTransformer2 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands
                .ofType<CreateNote>()
                .map { transformer2Result }
        }
    }

    private val mockTransformer3 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands
                .ofType<CauseError>()
                .flatMap { Observable.error<NotesResult>(error) }
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

    private val mockCommandWatcher1 = mock<Watcher<NotesCommand>>()

    private val mockCommandWatcher2 = mock<Watcher<NotesCommand>>()

    private val mockResultWatcher1 = mock<Watcher<NotesResult>>()

    private val mockResultWatcher2 = mock<Watcher<NotesResult>>()

    private val mockStateWatcher1 = mock<Watcher<NotesState>>()

    private val mockStateWatcher2 = mock<Watcher<NotesState>>()

    private val mockErrorWatcher1 = mock<Watcher<Throwable>>()

    private val mockErrorWatcher2 = mock<Watcher<Throwable>>()

    private val testScheduler = TestScheduler()

    private val impl = Store(
        initialState,
        listOf(mockTransformer1, mockTransformer2, mockTransformer3),
        listOf(mockReducer1, mockReducer2),
        listOf(mockCommandWatcher1, mockCommandWatcher2),
        listOf(mockResultWatcher1, mockResultWatcher2),
        listOf(mockStateWatcher1, mockStateWatcher2),
        listOf(mockErrorWatcher1, mockErrorWatcher2),
        testScheduler,
        false
    )

    private val implFaultTolerant = Store(
        initialState,
        listOf(mockTransformer1, mockTransformer2, mockTransformer3),
        listOf(mockReducer1, mockReducer2),
        listOf(mockCommandWatcher1, mockCommandWatcher2),
        listOf(mockResultWatcher1, mockResultWatcher2),
        listOf(mockStateWatcher1, mockStateWatcher2),
        listOf(mockErrorWatcher1, mockErrorWatcher2),
        testScheduler,
        true
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
    fun `command watchers are called in order`() {
        impl.subscribeInternal()
        val note = CreateNote(text)

        impl.issue(note)

        inOrder(mockCommandWatcher1, mockCommandWatcher2) {
            verify(mockCommandWatcher1).invoke(note)
            verify(mockCommandWatcher2).invoke(note)
        }
    }

    @Test
    fun `command watchers are called once per command even if multiple subscribers exist`() {
        impl.subscribeInternal()
        val note = CreateNote(text)
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(note)

        verify(mockCommandWatcher1).invoke(note)
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
    fun `result watchers are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(text))

        inOrder(mockResultWatcher1, mockResultWatcher2) {
            verify(mockResultWatcher1).invoke(transformer2Result)
            verify(mockResultWatcher2).invoke(transformer2Result)
        }
    }

    @Test
    fun `result watchers are called once per result even if multiple subscribers exist`() {
        impl.subscribeInternal()
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(text))

        verify(mockResultWatcher1).invoke(transformer2Result)
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
    fun `state watchers are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(text))

        inOrder(mockStateWatcher1, mockStateWatcher2) {
            verify(mockStateWatcher1).invoke(initialState)
            verify(mockStateWatcher2).invoke(initialState)
            verify(mockStateWatcher1).invoke(newState)
            verify(mockStateWatcher2).invoke(newState)
        }
    }

    @Test
    fun `state watchers are called once per state change even if multiple subscribers exist`() {
        impl.subscribeInternal()
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(text))

        inOrder(mockStateWatcher1) {
            verify(mockStateWatcher1).invoke(initialState)
            verify(mockStateWatcher1).invoke(newState)
        }
    }

    @Test
    fun `error watchers are called in order`() {
        impl.subscribeInternal()

        impl.issue(CauseError)

        inOrder(mockErrorWatcher1, mockErrorWatcher2) {
            verify(mockErrorWatcher1).invoke(error)
            verify(mockErrorWatcher2).invoke(error)
        }
    }

    @Test
    fun `error watchers are called once per error even if multiple subscribers exist`() {
        impl.subscribeInternal()
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CauseError)

        verify(mockErrorWatcher1).invoke(error)
    }

    @Test
    fun `error observed if not fault tolerant`() {
        impl.subscribeInternal()
        val observer = impl.states.test()

        impl.issue(CauseError)
        testScheduler.triggerActions()

        observer.assertError { true }
    }

    @Test
    fun `chain continues if fault tolerant`() {
        implFaultTolerant.subscribeInternal()
        val observer = implFaultTolerant.states.distinctUntilChanged().test()

        implFaultTolerant.issue(CauseError)
        implFaultTolerant.issue(CreateNote(text))
        testScheduler.triggerActions()

        observer.assertValues(initialState, newState)
    }
}
