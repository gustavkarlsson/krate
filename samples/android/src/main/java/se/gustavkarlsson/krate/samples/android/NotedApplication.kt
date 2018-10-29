package se.gustavkarlsson.krate.samples.android

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import se.gustavkarlsson.krate.samples.android.krate.NoteStore
import se.gustavkarlsson.krate.samples.android.krate.StreamNotes

class NotedApplication : Application() {

    private val store: NoteStore by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
        store.issue(StreamNotes)
    }
}
