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

abstract class Vcr<State : Any, Command : Any, Result : Any>(
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : StorePlugin<State, Command, Result> {
    private val playingSubject = PublishSubject.create<State>()
    private val recordingSubject = BehaviorSubject.create<State>()

    private var tape: Tape<State>? = null
    private var playingInProgress: Disposable? = null
    private var recordingInProgress: Disposable? = null
    private var lastSampleTime = 0L

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> {
        return interceptors + IgnoreIfPlaying()
    }

    override fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> {
        return interceptors + IgnoreIfPlaying()
    }

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> {
        return interceptors + Record() + Play()
    }

    @Synchronized
    fun record(name: String) {
        stop()
        lastSampleTime = currentTimeMillis()
        tape = newTape(name).also { tape ->
            recordingInProgress = recordingSubject
                .map { state ->
                    val delay = currentTimeMillis() - lastSampleTime
                    lastSampleTime += delay
                    Sample(state, delay)
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
        lastSampleTime = 0
    }

    @Synchronized
    fun play(name: String) {
        stop()
        tape = loadTape(name).also { loaded ->
            playingInProgress = loaded.play()
                .delay { Flowable.timer(it.delay, TimeUnit.MILLISECONDS) }
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

    protected abstract fun newTape(name: String): Tape<State>

    protected abstract fun loadTape(name: String): Tape<State>

    private inner class IgnoreIfPlaying<T> : Interceptor<T> {
        override fun invoke(items: Flowable<T>): Flowable<T> {
            return items.flatMapMaybe {
                if (playingInProgress != null) {
                    Maybe.empty()
                } else {
                    Maybe.just(it)
                }
            }
        }
    }

    private inner class Record : Interceptor<State> {
        override fun invoke(states: Flowable<State>): Flowable<State> {
            return states.doOnNext(recordingSubject::onNext)
        }
    }

    private inner class Play : Interceptor<State> {
        override fun invoke(states: Flowable<State>): Flowable<State> {
            return states.mergeWith(playingSubject.toFlowable(BackpressureStrategy.BUFFER))
        }
    }
}
