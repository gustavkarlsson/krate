package se.gustavkarlsson.krate.sample

import se.gustavkarlsson.krate.core.buildStore

fun main(args: Array<String>) {
    println("Starting sample app")

    val store = buildStore<State, Command, Result> {

        setInitialState(State())

        addTransformer<Command.AddNote> { commands, _ ->
            commands
                .flatMapSingle {
                    NotesApi.addNote(it.text, it.important)
                        .map<Result> { Result.NotesAdded(listOf(it)) }
                        .onErrorReturn { Result.Error(it) }
                }
        }

        addTransformer<Command.DeleteNote> { commands, _ ->
            commands
                .flatMapSingle {
                    val id = it.note.id
                    NotesApi.removeNote(id)
                        .toSingle<Result> { Result.NoteRemoved(id) }
                        .onErrorReturn { Result.Error(it) }
                }
        }

        addTransformer<Command.GetNotes> { commands, _ ->
            commands
                .flatMapSingle {
                    NotesApi.getNotes()
                        .map<Result> { Result.NotesAdded(it) }
                        .onErrorReturn { Result.Error(it) }
                }
        }

        addReducer<Result.NotesAdded> { state, result ->
            state.copy(notes = state.notes + result.notes)
        }

        addReducer<Result.NoteRemoved> { state, result ->
            state.copy(notes = state.notes.filter { it.id != result.id })
        }
    }
}
