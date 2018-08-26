package se.gustavkarlsson.krate.samples.android.gui.fragments.editnote

import se.gustavkarlsson.krate.samples.android.krate.NoteStore
import se.gustavkarlsson.krate.samples.android.krate.SetEditingNoteContent
import se.gustavkarlsson.krate.samples.android.krate.SetEditingNoteTitle

class EditNoteViewModel(private val store: NoteStore) {

    val initialTitle: String = store.currentState.editingNote!!.title

    val initialContent: String = store.currentState.editingNote!!.content

    fun onTitleChanged(text: CharSequence) {
        store.issue(SetEditingNoteTitle(text.toString()))
    }

    fun onContentChanged(text: CharSequence) {
        store.issue(SetEditingNoteContent(text.toString()))
    }
}
