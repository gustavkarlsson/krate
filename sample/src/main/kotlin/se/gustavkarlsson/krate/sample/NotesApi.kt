package se.gustavkarlsson.krate.sample

import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicLong

object NotesApi {
    private val notes = mutableListOf(
        Note(1, "Remember the milk", true),
        Note(2, "Wifi password: ihazNetwörk$", false)
    )
    private val nextId = AtomicLong(3)

    fun addNote(text: String, important: Boolean): Single<Note> {
        return if (text.hashCode() % 2 == 0) {
            Single.error(Exception("Failed to add note: '$text'"))
        } else {
            Single.just(Note(nextId.getAndIncrement(), text, important))
        }
    }

    fun removeNote(id: Long): Completable = Completable.fromCallable {
        notes.removeIf { it.id == id }
    }

    fun getNotes(): Single<List<Note>> = Single.just(ArrayList(notes))
}
