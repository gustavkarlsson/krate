package se.gustavkarlsson.krate.samples.android.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Note")
data class DbNote(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String
)
