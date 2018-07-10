package se.gustavkarlsson.krate.sample

import se.gustavkarlsson.krate.core.buildStore

val store = buildStore<State, Command, Result> {

    setInitialState(State())

    addTransformer<Command.AddNote> { commands, _ ->
        commands
            .flatMapSingle {
                NotesApi.addNote(it.text, it.important)
                    .map<Result> { Result.NotesAdded(listOf(it)) }
                    .onErrorReturn { Result.ErrorEncountered(it) }
            }
    }

    addTransformer<Command.DeleteNote> { commands, _ ->
        commands
            .flatMapSingle {
                val id = it.note.id
                NotesApi.removeNote(id)
                    .toSingle<Result> { Result.NoteRemoved(id) }
                    .onErrorReturn { Result.ErrorEncountered(it) }
            }
    }

    addTransformer<Command.GetNotes> { commands, _ ->
        commands
            .flatMapSingle {
                NotesApi.getNotes()
                    .map<Result> { Result.NotesAdded(it) }
                    .onErrorReturn { Result.ErrorEncountered(it) }
            }
    }

    addTransformer<Command.AcknowledgeError> { commands, _ ->
        commands
            .map {
                Result.ErrorAcknowledged(it.error)
            }
    }

    addReducer<Result.NotesAdded> { state, result ->
        state.copy(notes = state.notes + result.notes)
    }

    addReducer<Result.NoteRemoved> { state, result ->
        state.copy(notes = state.notes.filter { it.id != result.id })
    }

    addReducer<Result.ErrorEncountered> { state, result ->
        val message = result.throwable.message ?: "An unknown error has occurred"
        state.copy(errors = state.errors + message)
    }

    addReducer<Result.ErrorAcknowledged> { state, result ->
        state.copy(errors = state.errors - result.error)
    }
}
