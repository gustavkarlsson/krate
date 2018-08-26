package se.gustavkarlsson.krate.samples.android.gui

import io.reactivex.Flowable
import io.reactivex.Maybe
import se.gustavkarlsson.krate.samples.android.krate.NoteStore
import se.gustavkarlsson.krate.samples.android.krate.StopEditingNote

class MainViewModel(private val store: NoteStore) {

    val navigateToEditNote: Flowable<Unit> = store.states
        .distinctUntilChanged { a, b ->
            (a.editingNote != null) == (b.editingNote != null)
        }
        .flatMapMaybe {
            if (it.editingNote == null) {
                Maybe.empty()
            } else {
                Maybe.just(Unit)
            }
        }

    fun onNotNavigatingToEditNote() {
        store.issue(StopEditingNote)
    }
}
