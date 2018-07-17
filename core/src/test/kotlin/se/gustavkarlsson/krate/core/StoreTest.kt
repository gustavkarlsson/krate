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
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class StoreTest {
    private val initialState = NotesState()

    private val transformer2Result = NoteCreated(Note("transformer2 note"))

    private val reducer1Note = Note("reducer1 note")

    private val reducer2Note = Note("reducer2 note")

    private val newState = NotesState(listOf(reducer1Note, reducer2Note))

    private val mockTransformer1 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands.flatMap { Observable.empty<NotesResult>() }
        }
    }

    private val mockTransformer2 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands.map { transformer2Result }
        }
    }

    private val mockReducer1 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val state = it.arguments[0] as NotesState
            NotesState(state.notes + reducer1Note)
        }
    }

    private val mockReducer2 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val state = it.arguments[0] as NotesState
            NotesState(state.notes + reducer2Note)
        }
    }

    private val mockCommandWatcher1 = mock<Watcher<NotesCommand>>()

    private val mockCommandWatcher2 = mock<Watcher<NotesCommand>>()

    private val mockResultWatcher1 = mock<Watcher<NotesResult>>()

    private val mockResultWatcher2 = mock<Watcher<NotesResult>>()

    private val mockStateWatcher1 = mock<Watcher<NotesState>>()

    private val mockStateWatcher2 = mock<Watcher<NotesState>>()

    private val testScheduler = TestScheduler()

    private val impl = Store(
        initialState,
        listOf(mockTransformer1, mockTransformer2),
        listOf(mockReducer1, mockReducer2),
        listOf(mockCommandWatcher1, mockCommandWatcher2),
        listOf(mockResultWatcher1, mockResultWatcher2),
        listOf(mockStateWatcher1, mockStateWatcher2),
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
        impl.issue(CreateNote(""))

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
        impl.issue(CreateNote(""))
        testScheduler.triggerActions()

        val observer = impl.states.test()

        observer.assertValue(newState)
    }

    @Test
    fun `command watchers are called in order`() {
        impl.subscribeInternal()
        val note = CreateNote("")

        impl.issue(note)

        inOrder(mockCommandWatcher1, mockCommandWatcher2) {
            verify(mockCommandWatcher1).invoke(note)
            verify(mockCommandWatcher2).invoke(note)
        }
    }

    @Test
    fun `command watchers are called once per command even if multiple subscribers exist`() {
        impl.subscribeInternal()
        val note = CreateNote("")
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

        impl.issue(CreateNote(""))

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

        impl.issue(CreateNote(""))

        verify(mockResultWatcher1).invoke(transformer2Result)
    }

    @Test
    fun `reducers are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(""))

        inOrder(mockReducer1, mockReducer2) {
            verify(mockReducer1).invoke(initialState, transformer2Result)
            verify(mockReducer2).invoke(NotesState(initialState.notes + reducer1Note), transformer2Result)
        }
    }

    @Test
    fun `state watchers are called in order`() {
        impl.subscribeInternal()

        impl.issue(CreateNote(""))

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

        impl.issue(CreateNote(""))

        inOrder(mockStateWatcher1) {
            verify(mockStateWatcher1).invoke(initialState)
            verify(mockStateWatcher1).invoke(newState)
        }
    }
}
