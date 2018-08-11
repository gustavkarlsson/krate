package se.gustavkarlsson.krate.samples.tornadofx.store

import se.gustavkarlsson.krate.samples.tornadofx.models.Todo

data class State(
    val todos: List<Todo> = emptyList(),
    val creatingTodo: Boolean = false,
    val updatingTodoIds: Set<Int> = emptySet()
)
