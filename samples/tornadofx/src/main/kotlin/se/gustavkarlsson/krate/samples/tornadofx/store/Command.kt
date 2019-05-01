package se.gustavkarlsson.krate.samples.tornadofx.store

import se.gustavkarlsson.krate.samples.tornadofx.models.Todo

sealed class Command
data class CreateTodo(val text: String) : Command()
object CreatingNewTodo : Command()
data class CreatedNewTodo(val todo: Todo) : Command()
data class ToggleTodoCompleted(val todo: Todo) : Command()
data class UpdatingTodo(val id: Int) : Command()
data class DeleteTodo(val id: Int) : Command()
data class DeletedTodo(val id: Int) : Command()
data class ChangedTodo(val todo: Todo) : Command()
