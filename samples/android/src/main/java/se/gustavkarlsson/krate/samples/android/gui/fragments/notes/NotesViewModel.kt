package se.gustavkarlsson.krate.samples.android.gui.fragments.notes

import io.reactivex.Flowable
import se.gustavkarlsson.krate.samples.android.domain.Note
import se.gustavkarlsson.krate.samples.android.krate.DeleteNote
import se.gustavkarlsson.krate.samples.android.krate.NoteStore
import se.gustavkarlsson.krate.samples.android.krate.StartEditingNote

class NotesViewModel(private val store: NoteStore) {

    val notes: Flowable<List<Note>> = store.states
        .map { it.notes }
        .distinctUntilChanged()

    fun onAddNoteClicked() = store.issue(StartEditingNote(Note()))

    fun onNoteClicked(note: Note) = store.issue(StartEditingNote(note))

    fun onNoteSwiped(note: Note) = store.issue(DeleteNote(note))
}
