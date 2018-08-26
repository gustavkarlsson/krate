package se.gustavkarlsson.krate.samples.android.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(note: DbNote)

    @Update
    fun update(note: DbNote)

    @Delete
    fun delete(note: DbNote)

    @Query("SELECT * FROM Note")
    fun listAll(): Flowable<List<DbNote>>
}
