package se.gustavkarlsson.krate.samples.tornadofx.ui

import javafx.scene.control.ListView
import javafx.scene.control.TextField
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.listview
import tornadofx.onUserDelete
import tornadofx.onUserSelect
import tornadofx.selectWhere
import tornadofx.selectedItem
import tornadofx.textfield

class MainView : View("TODOs") {
    private val viewModel = MainViewModel()
    private lateinit var newTodoTextField: TextField
    private lateinit var todoList: ListView<TodoItem>

    override val root = borderpane {
        top = textfield().also { newTodoTextField = it }
        center = listview<TodoItem>().also { todoList = it }
    }

    init {
        bind()
    }

    private fun bind() {
        newTodoTextField.action {
            viewModel.add(newTodoTextField.text)
        }

        todoList.onUserSelect {
            viewModel.toggleCompleted(it)
        }

        todoList.onUserDelete {
            viewModel.delete(it)
        }

        viewModel.newTodoTextFieldEnabled
            .subscribe { enabled ->
                newTodoTextField.isDisable = !enabled
                if (enabled) {
                    newTodoTextField.requestFocus()
                } else {
                    newTodoTextField.clear()
                }
            }

        viewModel.newTodoTextFieldPromptText
            .subscribe {
                newTodoTextField.promptText = it
            }

        viewModel.items
            .subscribe { items ->
                val selected = todoList.selectedItem?.todo
                todoList.items.setAll(items)
                todoList.selectWhere { it.todo.id == selected?.id }
            }
    }
}
