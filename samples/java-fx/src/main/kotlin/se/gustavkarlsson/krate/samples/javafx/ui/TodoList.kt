package se.gustavkarlsson.krate.samples.javafx.ui

import se.gustavkarlsson.krate.samples.javafx.models.Todo
import se.gustavkarlsson.krate.samples.javafx.store.DeleteTodo
import se.gustavkarlsson.krate.samples.javafx.store.UpdateTodo
import se.gustavkarlsson.krate.samples.javafx.store.store
import tornadofx.View
import tornadofx.listview
import tornadofx.onUserDelete
import tornadofx.onUserSelect
import tornadofx.selectWhere
import tornadofx.selectedItem

class TodoList : View() {

    override val root = listview<TodoItem>()

    init {
        bindView()
        bindStore()
    }

    private fun bindView() {
        root.onUserSelect {
            it.todo.let { todo ->
                val changedTodo = todo.copy(isCompleted = !todo.isCompleted)
                store.issue(UpdateTodo(changedTodo))
            }
        }

        root.onUserDelete {
            store.issue(DeleteTodo(it.todo.id))
        }
    }

    private fun bindStore() {
        store.states
            .distinctUntilChanged { old, new -> old.todos == new.todos && old.updatingTodoIds == new.updatingTodoIds }
            .subscribe { state ->
                val selected = root.selectedItem?.todo
                val newItems = state.todos
                    .map { TodoItem(it, state.updatingTodoIds.contains(it.id)) }
                    .asReversed()
                root.items.setAll(newItems)
                root.selectWhere { it.todo.id == selected?.id }
            }
    }

    data class TodoItem(val todo: Todo, private val isUpdating: Boolean) {
        override fun toString(): String {
            val isCompletedText = if (todo.isCompleted) "[X]" else "[ ]"
            val isUpdatingText = if (isUpdating) " (updating)" else ""
            return "$isCompletedText ${todo.text}$isUpdatingText"
        }
    }
}
