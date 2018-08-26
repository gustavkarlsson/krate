package se.gustavkarlsson.krate.samples.android.database

import se.gustavkarlsson.krate.samples.android.domain.Note

fun DbNote.toEntity(): Note = Note(id, title, content)

fun Note.toDb(): DbNote = DbNote(id ?: 0, title, content)
