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

typealias NoteStore = Store<State, Command>

fun buildStore(dao: NoteDao): NoteStore = buildStore { getState ->

    commands {
        transform<StreamNotes> {
            observeOn(Schedulers.io())
                .flatMap { dao.listAll() }
                .map { it.map(DbNote::toEntity) }
                .map(::SetNotes)
        }

        transform<SaveEditingNote> {
            observeOn(Schedulers.io())
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

        transform<DeleteNote> {
            map(DeleteNote::note)
                .map(Note::toDb)
                .observeOn(Schedulers.io())
                .doOnNext(dao::delete)
                .flatMap<Command> { Flowable.empty() }
        }

        watchAll { Log.v("NoteStore", "Command: $it") }
    }

    states {
        initial = State()
        observeScheduler = AndroidSchedulers.mainThread()

        watchAll { Log.v("NoteStore", "State: $it") }

        reduce<SetNotes> { (notes) ->
            copy(notes = notes)
        }

        reduce<SetEditingNote> { (note) ->
            copy(editingNote = note)
        }

        reduce<SetEditingNoteTitle> { (newTitle) ->
            editingNote?.run {
                val newNote = copy(title = newTitle)
                copy(editingNote = newNote)
            } ?: this
        }

        reduce<SetEditingNoteContent> { (newContent) ->
            editingNote?.run {
                val newNote = copy(content = newContent)
                copy(editingNote = newNote)
            } ?: this
        }
    }
}
