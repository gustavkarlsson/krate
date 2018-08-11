package se.gustavkarlsson.krate.samples.javafx.ui

import io.reactivex.Flowable
import se.gustavkarlsson.krate.samples.javafx.store.AddTodo
import se.gustavkarlsson.krate.samples.javafx.store.DeleteTodo
import se.gustavkarlsson.krate.samples.javafx.store.UpdateTodo
import se.gustavkarlsson.krate.samples.javafx.store.store

class MainViewModel {

    fun add(text: String) {
        store.issue(AddTodo(text))
    }

    fun toggleCompleted(item: TodoItem) {
        item.todo.let { todo ->
            val changedTodo = todo.copy(isCompleted = !todo.isCompleted)
            store.issue(UpdateTodo(changedTodo))
        }
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
