package se.gustavkarlsson.krate.samples.javafx.ui

import javafx.scene.control.TextField
import se.gustavkarlsson.krate.samples.javafx.store.AddTodo
import se.gustavkarlsson.krate.samples.javafx.store.store
import tornadofx.View
import tornadofx.action
import tornadofx.textfield
import tornadofx.vbox

class Header : View() {

    lateinit var textField: TextField

    override val root = vbox {
        textField = textfield()
    }

    init {
        bindView()
        bindStore()
    }

    private fun bindView() {
        textField.action {
            store.issue(AddTodo(textField.text))
        }
    }

    private fun bindStore() {
        store.states
            .map { it.creatingTodo }
            .distinctUntilChanged()
            .subscribe { creating ->
                textField.disableProperty().set(creating)
                if (creating) {
                    textField.clear()
                    textField.promptText = "Adding..."
                } else {
                    textField.promptText = ""
                    textField.requestFocus()
                }
            }
    }
}
