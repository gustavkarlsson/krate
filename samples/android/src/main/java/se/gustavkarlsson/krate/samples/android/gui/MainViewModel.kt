package se.gustavkarlsson.krate.samples.android.gui

import se.gustavkarlsson.krate.samples.android.krate.NoteStore
import se.gustavkarlsson.krate.samples.android.krate.StopEditingNote

class MainViewModel(private val store: NoteStore) {

    fun onNotNavigatingToEditNote() {
        store.issue(StopEditingNote)
    }
}
