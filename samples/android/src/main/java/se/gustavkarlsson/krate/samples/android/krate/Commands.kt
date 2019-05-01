package se.gustavkarlsson.krate.samples.android.krate

import se.gustavkarlsson.krate.samples.android.domain.Note

sealed class Command

object StreamNotes : Command()

data class SetEditingNote(val note: Note?) : Command()

data class SetEditingNoteTitle(val text: String) : Command()

data class SetEditingNoteContent(val text: String) : Command()

object SaveEditingNote : Command()

data class DeleteNote(val note: Note) : Command()

data class SetNotes(val notes: List<Note>) : Command()
