package se.gustavkarlsson.krate.samples.tornadofx.ui

import io.reactivex.Flowable
import se.gustavkarlsson.krate.samples.tornadofx.store.CreateTodo
import se.gustavkarlsson.krate.samples.tornadofx.store.DeleteTodo
import se.gustavkarlsson.krate.samples.tornadofx.store.ToggleTodoCompleted
import se.gustavkarlsson.krate.samples.tornadofx.store.store

class MainViewModel {

    fun add(text: String) {
        store.issue(CreateTodo(text))
    }

    fun toggleCompleted(item: TodoItem) {
        store.issue(ToggleTodoCompleted(item.todo))
    }

    fun delete(item: TodoItem) {
        store.issue(DeleteTodo(item.todo.id))
    }

    val newTodoTextFieldEnabled: Flowable<Boolean> = store.states
        .map { it.creatingTodo }
        .distinctUntilChanged()
        .map(Boolean::not)

    val newTodoTextFieldPromptText: Flowable<String> = store.states
        .map { it.creatingTodo }
        .distinctUntilChanged()
        .map { creating ->
            if (creating) "Adding..." else ""
        }

    val items: Flowable<List<TodoItem>> = store.states
        .distinctUntilChanged { old, new -> old.todos == new.todos && old.updatingTodoIds == new.updatingTodoIds }
        .map { state ->
            state.todos
                .map { todo ->
                    val isUpdating = state.updatingTodoIds.contains(todo.id)
                    TodoItem(todo, isUpdating)
                }
                .asReversed()
        }
}
