package se.gustavkarlsson.krate.core

import Watcher
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class StoreTest {

    private val transformer1 = { commands: Observable<NotesCommand>, _: () -> NotesState ->
        commands
            .ofType<CreateNote>()
            .map { NoteCreated(Note(it.text)) }
            .cast<NotesResult>()
    }

    private val transformer2 = { commands: Observable<NotesCommand>, getState: () -> NotesState ->
        commands
            .ofType<GetNoteAsync>()
            .flatMapMaybe {
                if (getState().errors.isEmpty()) {
                    Maybe.just(NoteError(Exception("Failed to get notes")))
                } else {
                    Maybe.empty()
                }
            }
            .cast<NotesResult>()
    }

    private val reducer1 = { state: NotesState, result: NotesResult ->
        if (result is NoteCreated) {
            NotesState(state.notes + result.note)
        } else {
            state
        }
    }

    private val reducer2 = { state: NotesState, result: NotesResult ->
        if (result is NoteError) {
            NotesState(errors = state.errors + result.throwable.message!!)
        } else {
            state
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
        listOf(transformer1, transformer2),
        listOf(reducer1, reducer2),
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
        val note = CreateNote("testnote")

        impl.issue(note)

        inOrder(mockCommandWatcher1, mockCommandWatcher2) {
            verify(mockCommandWatcher1).invoke(note)
            verify(mockCommandWatcher2).invoke(note)
        }
    }

    @Test
    fun `command watchers are called once per command even if multiple subscribers exist`() {
        impl.start()
        val note = CreateNote("testnote")
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(note)

        verify(mockCommandWatcher1).invoke(note)
    }

    @Test
    fun `result watchers are called in order`() {
        impl.start()
        val noteText = "testnote"

        impl.issue(CreateNote(noteText))

        val expectedResult = NoteCreated(Note(noteText))
        inOrder(mockResultWatcher1, mockResultWatcher2) {
            verify(mockResultWatcher1).invoke(expectedResult)
            verify(mockResultWatcher2).invoke(expectedResult)
        }
    }

    @Test
    fun `result watchers are called once per result even if multiple subscribers exist`() {
        impl.start()
        val noteText = "testnote"
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(noteText))

        val expectedResult = NoteCreated(Note(noteText))
        verify(mockResultWatcher1).invoke(expectedResult)
    }

    @Test
    fun `state watchers are called in order`() {
        impl.start()
        val noteText = "testnote"

        impl.issue(CreateNote(noteText))

        val expectedState = NotesState(listOf(Note(noteText)))
        inOrder(mockStateWatcher1, mockStateWatcher2) {
            verify(mockStateWatcher1).invoke(expectedState)
            verify(mockStateWatcher2).invoke(expectedState)
        }
    }

    @Test
    fun `state watchers are called once per state change even if multiple subscribers exist`() {
        impl.start()
        val noteText = "testnote"
        impl.states.subscribe()
        impl.states.subscribe()

        impl.issue(CreateNote(noteText))

        val expectedState = NotesState(listOf(Note(noteText)))
        verify(mockStateWatcher1).invoke(expectedState)
    }
}
