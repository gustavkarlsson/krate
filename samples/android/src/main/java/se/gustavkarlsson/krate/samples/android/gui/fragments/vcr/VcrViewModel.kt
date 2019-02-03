package se.gustavkarlsson.krate.samples.android.gui.fragments.vcr

import se.gustavkarlsson.krate.vcr.Vcr

class VcrViewModel(private val vcr: Vcr<*, *, *, String>) {

    private var recordingExists = false

    fun onStopClicked() {
        vcr.stop()
    }

    fun onRecordClicked() {
        vcr.stop()
        if (recordingExists) vcr.erase(TAPE_NAME)
        vcr.record(TAPE_NAME)
        recordingExists = true
    }

    fun onPlayClicked() {
        vcr.stop()
        vcr.play(TAPE_NAME)
    }

    companion object {
        private const val TAPE_NAME = "demo"
    }
}
