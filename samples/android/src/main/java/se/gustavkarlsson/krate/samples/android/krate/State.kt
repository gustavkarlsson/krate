package se.gustavkarlsson.krate.samples.android.krate

import se.gustavkarlsson.krate.samples.android.domain.Note

data class State(val notes: List<Note> = emptyList(), val editingNote: Note? = null)
