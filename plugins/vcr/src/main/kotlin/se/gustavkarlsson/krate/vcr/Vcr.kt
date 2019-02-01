package se.gustavkarlsson.krate.vcr

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin
import java.util.concurrent.TimeUnit

abstract class Vcr<State : Any, TapeId>(
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : StorePlugin<State, Any, Any> {
    private val playingSubject = PublishSubject.create<State>()
    private val recordingSubject = BehaviorSubject.create<State>()

    private var tape: Tape<State>? = null
    private var playingInProgress: Disposable? = null
    private var recordingInProgress: Disposable? = null
    private var startTime = 0L

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeResultInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> =
        interceptors + Record() + Play()

    @Synchronized
    fun record(tapeId: TapeId) {
        stop()
        startTime = currentTimeMillis()
        tape = newTape(tapeId).also { tape ->
            recordingInProgress = recordingSubject
                .map { state ->
                    val timestamp = currentTimeMillis() - startTime
                    Sample(state, timestamp)
                }
                .subscribe(tape::append)
        }
    }

    @Synchronized
    fun stop() {
        playingInProgress?.dispose()
        playingInProgress = null
        recordingInProgress?.dispose()
        recordingInProgress = null
        tape?.stop()
        tape = null
        startTime = 0
    }

    @Synchronized
    fun play(tapeId: TapeId) {
        stop()
        tape = loadTape(tapeId).also { tape ->
            playingInProgress = tape.play()
                .delay { Flowable.timer(it.timestamp, TimeUnit.MILLISECONDS) }
                .map { it.state }
                .subscribe(playingSubject::onNext)
        }
    }

    val isRecording: Boolean
        get() = recordingInProgress != null

    val isPlaying: Boolean
        get() = playingInProgress != null

    val isStopped: Boolean
        get() = !isRecording && !isPlaying

    protected abstract fun newTape(tapeId: TapeId): Tape<State>

    protected abstract fun loadTape(tapeId: TapeId): Tape<State>

    private inner class IgnoreIfPlaying<T> : Interceptor<T> {
        override fun invoke(items: Flowable<T>): Flowable<T> =
            items.flatMapMaybe {
                if (isPlaying) Maybe.empty() else Maybe.just(it)
            }
    }

    private inner class Record : Interceptor<State> {
        override fun invoke(states: Flowable<State>): Flowable<State> =
            states.doOnNext(recordingSubject::onNext)
    }

    private inner class Play : Interceptor<State> {
        override fun invoke(states: Flowable<State>): Flowable<State> =
            states.mergeWith(playingSubject.toFlowable(BackpressureStrategy.BUFFER))
    }
}
