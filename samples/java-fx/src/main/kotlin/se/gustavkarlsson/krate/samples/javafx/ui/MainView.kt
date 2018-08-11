package se.gustavkarlsson.krate.samples.javafx.ui

import javafx.scene.control.ListView
import javafx.scene.control.TextField
import se.gustavkarlsson.krate.samples.javafx.models.Todo
import se.gustavkarlsson.krate.samples.javafx.store.AddTodo
import se.gustavkarlsson.krate.samples.javafx.store.DeleteTodo
import se.gustavkarlsson.krate.samples.javafx.store.UpdateTodo
import se.gustavkarlsson.krate.samples.javafx.store.store
import tornadofx.View
import tornadofx.action
import tornadofx.listview
import tornadofx.onUserDelete
import tornadofx.onUserSelect
import tornadofx.selectWhere
import tornadofx.selectedItem
import tornadofx.textfield
import tornadofx.vbox

class MainView : View("TODOs") {
    private lateinit var newTodoTextField: TextField
    private lateinit var todoList: ListView<TodoItem>

    override val root = vbox {
        newTodoTextField = textfield()
        todoList = listview()
    }

    init {
        bindData()
    }

    private fun bindData() {
        newTodoTextField.action {
            store.issue(AddTodo(newTodoTextField.text))
        }

        todoList.onUserSelect {
            it.todo.let { todo ->
                val changedTodo = todo.copy(isCompleted = !todo.isCompleted)
                store.issue(UpdateTodo(changedTodo))
            }
        }

        todoList.onUserDelete {
            store.issue(DeleteTodo(it.todo.id))
        }

        store.states
            .map { it.creatingTodo }
            .distinctUntilChanged()
            .subscribe { creating ->
                newTodoTextField.isDisable = creating
                if (creating) {
                    newTodoTextField.clear()
                    newTodoTextField.promptText = "Adding..."
                } else {
                    newTodoTextField.promptText = ""
                    newTodoTextField.requestFocus()
                }
            }

        store.states
            .distinctUntilChanged { old, new -> old.todos == new.todos && old.updatingTodoIds == new.updatingTodoIds }
            .subscribe { state ->
                val selected = todoList.selectedItem?.todo
                val newItems = state.todos
                    .map { TodoItem(it, state.updatingTodoIds.contains(it.id)) }
                    .asReversed()
                todoList.items.setAll(newItems)
                todoList.selectWhere { it.todo.id == selected?.id }
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
