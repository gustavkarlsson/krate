package se.gustavkarlsson.krate.samples.android

import androidx.room.Room
import org.koin.dsl.module.module
import se.gustavkarlsson.krate.samples.android.database.NotedDatabase
import se.gustavkarlsson.krate.samples.android.gui.MainViewModel
import se.gustavkarlsson.krate.samples.android.gui.fragments.editnote.EditNoteViewModel
import se.gustavkarlsson.krate.samples.android.gui.fragments.notes.NotesViewModel
import se.gustavkarlsson.krate.samples.android.krate.buildStore

val appModule = module {

    single { buildStore(get()) }

    single {
        Room.databaseBuilder(get(), NotedDatabase::class.java, "notes")
            .build()
            .getNoteDao()
    }

    factory { EditNoteViewModel(get()) }

    factory { NotesViewModel(get()) }

    factory { MainViewModel(get()) }
}
