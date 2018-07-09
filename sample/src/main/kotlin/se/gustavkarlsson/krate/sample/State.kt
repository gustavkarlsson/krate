package se.gustavkarlsson.krate.sample

data class State(
    val user: User? = null,
    val notes: List<Note> = emptyList(),
    val errors: List<String> = emptyList()
)
