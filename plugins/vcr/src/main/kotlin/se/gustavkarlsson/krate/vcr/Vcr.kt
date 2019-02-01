package se.gustavkarlsson.krate.vcr

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin
import java.util.concurrent.TimeUnit

private sealed class Command<TapeId> {
    object Stop : Command<Nothing>()
    data class Record<TapeId>(val tapeId: TapeId, val startTime: Long) : Command<TapeId>()
    data class Play<TapeId>(val tapeId: TapeId) : Command<TapeId>()
    data class Erase<TapeId>(val tapeId: TapeId) : Command<TapeId>()
}

abstract class Vcr<State : Any, TapeId>(
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : StorePlugin<State, Any, Any> {
    private val commands = PublishSubject.create<Command<TapeId>>().serialize()
    private val playingSubject = PublishSubject.create<State>()
    private val recordingSubject = BehaviorSubject.create<State>()

    private var recording: Disposable? = null
    private var playback: Disposable? = null
    private var startTime = 0L

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeResultInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> =
        interceptors + Record() + Play()

    init {
        commands
            .switchMapCompletable {
                when (it) {
                    is Command.Stop -> Completable.complete()
                    is Command.Record -> doRecord(it.tapeId)
                    is Command.Play -> doPlay(it.tapeId)
                    is Command.Erase -> doErase(it.tapeId)
                }
            }
            .subscribe()
    }

    private fun doRecord(tapeId: TapeId): Completable {
        startTime = currentTimeMillis()
        recording = startRecording(tapeId)
            .flatMapCompletable { recording ->
                recordingSubject
                    .map { state ->
                        val timestamp = currentTimeMillis() - startTime
                        Sample(state, timestamp)
                    }
                    .concatMapCompletable(recording::write)
                    .doOnDispose(recording::dispose)
            }
            .subscribe()
    }

    fun doPlay(tapeId: TapeId): Completable {
        playback = startPlaying(tapeId)
            .delay { Flowable.timer(it.timestamp, TimeUnit.MILLISECONDS) }
            .map { it.state }
            .subscribe(playingSubject::onNext)
    }

    fun doErase(tapeId: TapeId): Completable {
        eraseTape(tapeId)
    }

    val isRecording: Boolean
        get() = recording != null

    val isPlaying: Boolean
        get() = playback != null

    val isStopped: Boolean
        get() = !isRecording && !isPlaying

    protected abstract fun startRecording(tapeId: TapeId): Single<Recording<State>>

    protected abstract fun startPlaying(tapeId: TapeId): Flowable<Sample<State>>

    protected abstract fun eraseTape(tapeId: TapeId): Completable

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
