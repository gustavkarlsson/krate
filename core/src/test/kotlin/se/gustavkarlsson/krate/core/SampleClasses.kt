package se.gustavkarlsson.krate.core

data class Note(val text: String)

data class NotesState(
    val notes: List<Note> = emptyList()
)

sealed class NotesCommand
data class CreateNote(val text: String) : NotesCommand()
object CauseError : NotesCommand()

sealed class NotesResult
data class NoteCreated(val note: Note) : NotesResult()
