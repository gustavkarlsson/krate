package se.gustavkarlsson.krate.samples.tornadofx.ui

import se.gustavkarlsson.krate.samples.tornadofx.models.Todo

data class TodoItem(val todo: Todo, private val isUpdating: Boolean) {
    override fun toString(): String {
        val isCompletedText = if (todo.isCompleted) "[X]" else "[ ]"
        val isUpdatingText = if (isUpdating) " (updating)" else ""
        return "$isCompletedText ${todo.text}$isUpdatingText"
    }
}
