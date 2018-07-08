package se.gustavkarlsson.krate.sample

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import se.gustavkarlsson.krate.core.buildStore
import java.util.concurrent.TimeUnit

// Sample data
data class Note(val text: String)

// Sample API
class NotesApi {
    fun getNote(id: Int): Single<Note> {
        return if (id % 2 == 0) {
            Single.error(Exception("Failed to get note from API"))
        } else {
            Single.just(Note("I came from the API"))
        }
    }
}

// State
data class NotesState(
    val notes: List<Note> = emptyList(),
    val errors: List<String> = emptyList()
)

// Commands
sealed class NotesCommand

data class CreateNote(val text: String) : NotesCommand()
data class GetNoteAsync(val id: Int) : NotesCommand()
data class IncrementallyCreateNotes(val limit: Int) : NotesCommand()

// Results
sealed class NotesResult

data class NoteCreated(val note: Note) : NotesResult()
data class NoteError(val throwable: Throwable) : NotesResult()

fun main(args: Array<String>) {
    println("Starting sample app")
    val notesApi = NotesApi()

    val store = buildStore<NotesState, NotesCommand, NotesResult> {

        setInitialState(NotesState())

        // Every CreateNote results in one NoteCreated
        addTransformer<CreateNote> { commands, _ ->
            commands
                .map { Note(it.text) }
                .map { NoteCreated(it) }
        }

        // Every CreateNote makes an async call which eventually results in a NoteCreated OR an Error
        addTransformer<GetNoteAsync> { commands, _ ->
            commands
                .flatMapSingle { notesApi.getNote(it.id) }
                .map<NotesResult> { NoteCreated(it) }
                .onErrorReturn { NoteError(it) }
        }

        // When IncrementallyCreateNotes is emitted,
        // keeps adding notes with a 1s interval
        // as long as the number of notes is less than the specified limit
        addTransformer<IncrementallyCreateNotes> { commands, getState ->
            commands
                .switchMap { command ->
                    Observable.interval(1, TimeUnit.SECONDS)
                        .flatMapMaybe {
                            if (getState().notes.size < command.limit) {
                                Maybe.just(Note(it.toString()))
                            } else {
                                Maybe.empty()
                            }
                        }
                        .map<NotesResult> {
                            NoteCreated(it)
                        }
                }
        }

        addReducer<NoteCreated> { state, result ->
            state.copy(notes = state.notes + result.note)
        }

        addReducer<NoteError> { state, result ->
            val message = result.throwable.message ?: "Unknown error"
            state.copy(errors = state.errors + message)
        }

        addCommandListener<NotesCommand> { println("Command: $it") }
        addResultListener<NotesResult> { println("Result: $it") }
        addStateListener { println("State: $it") }
    }

    store.start()
    store.issue(IncrementallyCreateNotes(3))
    Thread.sleep(4500)
    store.issue(GetNoteAsync(4))
    store.issue(IncrementallyCreateNotes(10))

    Thread.sleep(1000000)
}
