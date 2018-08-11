package se.gustavkarlsson.krate.samples.javafx

import javafx.application.Application
import se.gustavkarlsson.krate.samples.javafx.ui.MainView
import tornadofx.App

fun main(args: Array<String>) {
    Application.launch(TodoApp::class.java, *args)
}

class TodoApp : App(MainView::class)
