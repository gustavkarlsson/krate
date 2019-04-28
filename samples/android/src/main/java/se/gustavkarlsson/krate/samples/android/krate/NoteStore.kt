package se.gustavkarlsson.krate.samples.android.krate

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.gustavkarlsson.krate.core.Store
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.android.database.DbNote
import se.gustavkarlsson.krate.samples.android.database.NoteDao
import se.gustavkarlsson.krate.samples.android.database.toDb
import se.gustavkarlsson.krate.samples.android.database.toEntity
import se.gustavkarlsson.krate.samples.android.domain.Note

typealias NoteStore = Store<State, Command, Result>

fun buildStore(dao: NoteDao): NoteStore = buildStore { getState ->

    commands {
        transform<StreamNotes> { commands ->
            commands
                .observeOn(Schedulers.io())
                .flatMap { dao.listAll() }
                .map { it.map(DbNote::toEntity) }
                .map(::Notes)
        }

        transform<StartEditingNote> { commands ->
            commands
                .map(StartEditingNote::note)
                .map(::SetEditingNote)
        }

        transform<SetEditingNoteTitle> { commands ->
            commands
                .map { ChangeEditingNote(title = it.text) }
        }

        transform<SetEditingNoteContent> { commands ->
            commands
                .map { ChangeEditingNote(content = it.text) }
        }

        transform<StopEditingNote> { commands ->
            commands
                .observeOn(Schedulers.io())
                .doOnNext {
                    getState().editingNote?.let { editingNote ->
                        if (editingNote.title.isBlank() && editingNote.content.isBlank()) {
                            if (editingNote.id != null) {
                                dao.delete(editingNote.toDb())
                            }
                        } else {
                            if (editingNote.id != null) {
                                dao.update(editingNote.toDb())
                            } else {
                                dao.insert(editingNote.toDb())
                            }
                        }
                    }
                }
                .map { SetEditingNote(null) }
        }

        transform<DeleteNote> { commands ->
            commands
                .map(DeleteNote::note)
                .map(Note::toDb)
                .observeOn(Schedulers.io())
                .doOnNext(dao::delete)
                .flatMap<Result> { Flowable.empty() }
        }

        watchAll { Log.v("NoteStore", "Command: $it") }
    }

    results {
        reduce { state, result ->
            when (result) {
                is Notes -> state.copy(notes = result.notes)
                is SetEditingNote -> state.copy(editingNote = result.note)
                is ChangeEditingNote -> {
                    state.editingNote?.run {
                        val newNote = copy(title = result.title ?: title, content = result.content ?: content)
                        state.copy(editingNote = newNote)
                    } ?: state
                }
            }
        }

        watchAll { Log.v("NoteStore", "Result: $it") }
    }

    states {
        initial = State()
        observeScheduler = AndroidSchedulers.mainThread()

        watchAll { Log.v("NoteStore", "State: $it") }
    }
}
