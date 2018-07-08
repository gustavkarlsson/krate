package se.gustavkarlsson.krate.core

data class Note(val text: String)

data class NotesState(
    val notes: List<Note> = emptyList(),
    val errors: List<String> = emptyList()
)

sealed class NotesCommand
data class CreateNote(val text: String) : NotesCommand()
object GetNoteAsync : NotesCommand()
data class IncrementallyCreateNotes(val limit: Int) : NotesCommand()

sealed class NotesResult
data class NoteCreated(val note: Note) : NotesResult()
data class NoteError(val throwable: Throwable) : NotesResult()
