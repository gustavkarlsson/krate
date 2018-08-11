package se.gustavkarlsson.krate.samples.javafx.repo

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import se.gustavkarlsson.krate.samples.javafx.models.Todo
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TodoRepository {

    private val nextId = AtomicInteger(1)

    private val random = Random()

    private val data = mutableListOf<Todo>()

    fun add(text: String, isCompleted: Boolean): Single<Todo> {
        return Single.fromCallable { addInternal(text, isCompleted) }
            .delay(randomDelay(), TimeUnit.MILLISECONDS, Schedulers.io())
    }

    private fun addInternal(text: String, isCompleted: Boolean): Todo {
        return Todo(nextId.getAndIncrement(), text, isCompleted).also { data += it }
    }

    fun update(todo: Todo): Single<Todo> {
        return Single.fromCallable { updateInternal(todo) }
            .delay(randomDelay(), TimeUnit.MILLISECONDS, Schedulers.io())
    }

    private fun updateInternal(todo: Todo): Todo {
        data.replaceAll { existing ->
            if (existing.id == todo.id) {
                todo
            } else {
                existing
            }
        }
        return todo
    }

    fun delete(id: Int): Completable {
        return Completable.fromRunnable { removeInternal(id) }
            .delay(randomDelay(), TimeUnit.MILLISECONDS, Schedulers.io())
    }

    private fun removeInternal(id: Int) {
        data.removeIf { existing -> existing.id == id }
    }

    private fun randomDelay() = random.nextInt(3000).toLong()
}
