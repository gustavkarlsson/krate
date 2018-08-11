package se.gustavkarlsson.krate.samples.tornadofx

import javafx.application.Application
import se.gustavkarlsson.krate.samples.tornadofx.ui.MainView
import tornadofx.App

fun main(args: Array<String>) {
    Application.launch(TodoApp::class.java, *args)
}

class TodoApp : App(MainView::class)
