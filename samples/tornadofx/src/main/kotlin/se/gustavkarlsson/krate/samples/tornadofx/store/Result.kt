package se.gustavkarlsson.krate.samples.tornadofx.store

import se.gustavkarlsson.krate.samples.tornadofx.models.Todo

sealed class Result
object CreatingNewTodo : Result()
data class CreatedNewTodo(val todo: Todo) : Result()
data class UpdatingTodo(val id: Int) : Result()
data class ChangedTodo(val todo: Todo) : Result()
data class DeletedTodo(val id: Int) : Result()
