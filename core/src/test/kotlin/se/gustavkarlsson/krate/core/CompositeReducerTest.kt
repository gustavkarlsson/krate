package se.gustavkarlsson.krate.core

import Reducer
import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CompositeReducerTest {

    private val initialState = NotesState()
    private val notesState1 =
        NotesState(listOf(Note("first")))
    private val notesState2 =
        NotesState(listOf(Note("second")))
    private val result = NoteCreated(Note("reminder!"))

    private val mockReducer1 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenReturn(notesState1)
    }

    private val mockReducer2 = mock<Reducer<NotesState, NotesResult>> {
        on(it.invoke(any(), any())).thenReturn(notesState2)
    }

    @Test
    fun `returns result of last reducer`() {
        val reducer = CompositeReducer(listOf(mockReducer1, mockReducer2))

        val reduced = reducer(initialState, result)

        assert(reduced).isEqualTo(notesState2)
    }

    @Test
    fun `calls all reducers in order`() {
        val reducer = CompositeReducer(listOf(mockReducer1, mockReducer2))

        reducer(initialState, result)

        inOrder(mockReducer1, mockReducer2) {
            verify(mockReducer1).invoke(initialState, result)
            verify(mockReducer2).invoke(notesState1, result)
        }
    }
}
