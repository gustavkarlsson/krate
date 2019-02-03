package se.gustavkarlsson.krate.vcr.implementations

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import se.gustavkarlsson.krate.vcr.Playback
import se.gustavkarlsson.krate.vcr.Recording
import se.gustavkarlsson.krate.vcr.Sample
import se.gustavkarlsson.krate.vcr.Vcr
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.file.Files

class FileBasedVcr<State : Any, Command : Any, Result : Any>(
    private val serializer: Serializer<State>
) : Vcr<State, Command, Result, File>() {

    interface Serializer<State> {
        fun serialize(sample: Sample<State>): ByteArray
        fun deserialize(bytes: ByteArray): Sample<State>
    }

    override fun startRecording(tapeId: File): Single<Recording<State>> =
        Single.fromCallable {
            require(tapeId.createNewFile()) { "File already exists: $tapeId" }
            val stream = DataOutputStream(tapeId.outputStream().buffered())
            FileRecording(stream, serializer)
        }

    override fun startPlaying(tapeId: File): Single<Playback<State>> =
        Single.fromCallable {
            require(tapeId.isFile) { "Not a file: $tapeId" }
            val stream = DataInputStream(tapeId.inputStream().buffered())
            FilePlayback(stream, serializer)
        }

    override fun eraseTape(tapeId: File): Completable =
        Completable.fromAction {
            require(tapeId.isFile) { "Not a file: $tapeId" }
            Files.delete(tapeId.toPath())
        }

    private inner class FileRecording<State : Any>(
        private val stream: DataOutputStream,
        private val serializer: Serializer<State>
    ) : Recording<State> {
        private var disposed = false

        override val input: Consumer<Sample<State>> = Consumer {
            val bytes = serializer.serialize(it)
            stream.writeInt(bytes.size)
            stream.write(bytes)
        }

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            if (!disposed) {
                stream.flush()
                stream.close()
            }
            disposed = true
        }
    }

    private inner class FilePlayback<State : Any>(
        private val stream: DataInputStream,
        private val serializer: Serializer<State>
    ) : Playback<State> {
        private var disposed = false

        override val output: Iterable<Sample<State>> = Iterable {
            object : Iterator<Sample<State>> {

                override fun next(): Sample<State> {
                    val size = stream.readInt()
                    val bytes = ByteArray(size)
                    stream.read(bytes)
                    return serializer.deserialize(bytes)
                }

                override fun hasNext(): Boolean = stream.available() > 0
            }
        }

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            if (!disposed) {
                stream.close()
            }
            disposed = true
        }
    }
}
