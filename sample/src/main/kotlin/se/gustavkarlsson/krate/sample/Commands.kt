package se.gustavkarlsson.krate.sample

sealed class Command {
    object GetNotes : Command()
    data class AddNote(val text: String, val important: Boolean = false) : Command()
    data class DeleteNote(val note: Note) : Command()
    data class AcknowledgeError(val error: String): Command()
}
