package se.gustavkarlsson.krate.sample

import se.gustavkarlsson.krate.core.buildStore

val store = buildStore<State, Command, Result> {

    setInitialState(State())

    transformByType<Command.AddNote> {
        flatMapSingle {
            NotesApi.addNote(it.text, it.important)
                .map<Result> { Result.NotesAdded(listOf(it)) }
                .onErrorReturn { Result.ErrorEncountered(it) }
        }
    }

    transformByType<Command.DeleteNote> {
        flatMapSingle {
            val id = it.note.id
            NotesApi.removeNote(id)
                .toSingle<Result> { Result.NoteRemoved(id) }
                .onErrorReturn { Result.ErrorEncountered(it) }
        }
    }

    transformByType<Command.GetNotes> {
        flatMapSingle {
            NotesApi.getNotes()
                .map<Result> { Result.NotesAdded(it) }
                .onErrorReturn { Result.ErrorEncountered(it) }
        }
    }

    transformByType<Command.AcknowledgeError> {
        map {
            Result.ErrorAcknowledged(it.error)
        }
    }

    reduceByType<Result.NotesAdded> { state, result ->
        state.copy(notes = state.notes + result.notes)
    }

    reduceByType<Result.NoteRemoved> { state, result ->
        state.copy(notes = state.notes.filter { it.id != result.id })
    }

    reduceByType<Result.ErrorEncountered> { state, result ->
        val message = result.throwable.message ?: "An unknown error has occurred"
        state.copy(errors = state.errors + message)
    }

    reduceByType<Result.ErrorAcknowledged> { state, result ->
        state.copy(errors = state.errors - result.error)
    }
}
