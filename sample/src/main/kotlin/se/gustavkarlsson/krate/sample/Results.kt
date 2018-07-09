package se.gustavkarlsson.krate.sample


sealed class Result {
    data class NotesAdded(val notes: List<Note>) : Result()
    data class NoteRemoved(val id: Long) : Result()
    data class Error(val throwable: Throwable) : Result()
}
