package se.gustavkarlsson.krate.samples.javafx.ui

import javafx.scene.control.TextField
import se.gustavkarlsson.krate.samples.javafx.store.AddTodo
import se.gustavkarlsson.krate.samples.javafx.store.store
import tornadofx.View
import tornadofx.action
import tornadofx.textfield
import tornadofx.vbox

class Header : View() {

    lateinit var newTodoTextField: TextField

    override val root = vbox {
        newTodoTextField = textfield()
    }

    init {
        bindView()
        bindStore()
    }

    private fun bindView() {
        newTodoTextField.action {
            store.issue(AddTodo(newTodoTextField.text))
        }
    }

    private fun bindStore() {
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
    }
}
