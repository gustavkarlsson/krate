package se.gustavkarlsson.krate.sample


sealed class Result {
    data class NotesAdded(val notes: List<Note>) : Result()
    data class NoteRemoved(val id: Long) : Result()
    data class ErrorEncountered(val throwable: Throwable) : Result()
    data class ErrorAcknowledged(val error: String) : Result()
}
