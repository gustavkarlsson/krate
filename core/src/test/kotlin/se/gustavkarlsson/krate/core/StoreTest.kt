package se.gustavkarlsson.krate.core

import Reducer
import Transformer
import Watcher
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class StoreTest {
    private val transformerNote2 = Note("transformer2 note")

    private val reducer1Note = Note("reducer1 note")

    private val reducer2Note = Note("reducer2 note")

    private val mockTransformer1 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands.flatMap { Observable.empty<NotesResult>() }
        }
    }

    private val mockTransformer2 = mock<Transformer<NotesState, NotesCommand, NotesResult>> {
        on(it.invoke(any(), any())).thenAnswer {
            val commands = it.arguments[0] as Observable<*>
            commands.map { NoteCreated(transformerNote2) }
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
        NotesState(),
        listOf(mockTransformer1, mockTransformer2),
        listOf(mockReducer1, mockReducer2),
        listOf(mockCommandWatcher1, mockCommandWatcher2),
        listOf(mockResultWatcher1, mockResultWatcher2),
        listOf(mockStateWatcher1, mockStateWatcher2),
        testScheduler
    )

    @Test
    fun `subscribe before any command gets initial state`() {
        impl.start()

        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(NotesState())
    }

    @Test
    fun `second subscribe before any command also gets initial state`() {
        impl.start()

        impl.states.subscribe()
        val observer = impl.states.test()
        testScheduler.triggerActions()

        observer.assertValue(NotesState())
    }

    @Test
    fun `command watchers are called in order`() {
        impl.start()
        val note = CreateNote("")

        impl.issue(note)

        inOrder(mockCommandWatcher1, mockCommandWatcher2) {
            verify(mockCommandWatcher1).invoke(note)
            verify(mockCommandWatcher2).invoke(note)
        }
    }

    @Test
    fun `command watchers are called once per command even if multiple subscribers exist`() {
        impl.start()
        val note = CreateNote("")
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(note)

        verify(mockCommandWatcher1).invoke(note)
    }

    @Test
    fun `result watchers are called in order`() {
        impl.start()

        impl.issue(CreateNote(""))

        val expectedResult = NoteCreated(transformerNote2)
        inOrder(mockResultWatcher1, mockResultWatcher2) {
            verify(mockResultWatcher1).invoke(expectedResult)
            verify(mockResultWatcher2).invoke(expectedResult)
        }
    }

    @Test
    fun `result watchers are called once per result even if multiple subscribers exist`() {
        impl.start()
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(""))

        val expectedResult = NoteCreated(transformerNote2)
        verify(mockResultWatcher1).invoke(expectedResult)
    }

    @Test
    fun `state watchers are called in order`() {
        impl.start()

        impl.issue(CreateNote(""))

        val expectedState1 = NotesState(listOf())
        val expectedState2 = NotesState(listOf(reducer1Note, reducer2Note))
        inOrder(mockStateWatcher1, mockStateWatcher2) {
            verify(mockStateWatcher1).invoke(expectedState1)
            verify(mockStateWatcher2).invoke(expectedState1)
            verify(mockStateWatcher1).invoke(expectedState2)
            verify(mockStateWatcher2).invoke(expectedState2)
        }
    }

    @Test
    fun `state watchers are called once per state change even if multiple subscribers exist`() {
        impl.start()
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(""))

        inOrder(mockStateWatcher1) {
            verify(mockStateWatcher1).invoke(NotesState(listOf()))
            verify(mockStateWatcher1).invoke(NotesState(listOf(reducer1Note, reducer2Note)))
        }
    }
}
