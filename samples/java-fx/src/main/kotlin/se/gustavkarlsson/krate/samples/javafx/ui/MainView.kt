package se.gustavkarlsson.krate.samples.javafx.ui

import tornadofx.View
import tornadofx.borderpane

class MainView : View("TODOs") {
    override val root = borderpane {
        top(Header::class)
        center(TodoList::class)
    }
}
