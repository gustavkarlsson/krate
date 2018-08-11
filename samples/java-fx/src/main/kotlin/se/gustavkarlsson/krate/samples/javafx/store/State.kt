package se.gustavkarlsson.krate.samples.javafx.store

import se.gustavkarlsson.krate.samples.javafx.models.Todo

data class State(
    val todos: List<Todo> = emptyList(),
    val creatingTodo: Boolean = false,
    val updatingTodoIds: Set<Int> = emptySet()
)
