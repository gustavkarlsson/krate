package se.gustavkarlsson.krate.samples.tornadofx.store

import se.gustavkarlsson.krate.samples.tornadofx.models.Todo

sealed class Command
data class AddTodo(val text: String) : Command()
data class UpdateTodo(val todo: Todo) : Command()
data class DeleteTodo(val id: Int) : Command()
