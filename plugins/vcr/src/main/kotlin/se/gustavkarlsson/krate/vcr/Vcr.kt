package se.gustavkarlsson.krate.vcr

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.dsl.StorePlugin
import java.util.concurrent.TimeUnit

private sealed class Action<TapeId> {
    class Stop<TapeId> : Action<TapeId>()
    data class Record<TapeId>(val tapeId: TapeId, val startTime: Long) : Action<TapeId>()
    data class Play<TapeId>(val tapeId: TapeId) : Action<TapeId>()
    data class Erase<TapeId>(val tapeId: TapeId) : Action<TapeId>()
}

abstract class Vcr<State : Any, Command : Any, Result : Any, TapeId>(
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : StorePlugin<State, Command, Result> {
    private val commands = PublishSubject.create<Action<TapeId>>().toSerialized()
    private val playingSubject = PublishSubject.create<State>()
    private val recordingSubject = BehaviorSubject.create<State>()

    init {
        commands
            .switchMapCompletable {
                when (it) {
                    is Action.Stop -> Completable.complete()
                    is Action.Record -> doRecord(it.tapeId, it.startTime)
                    is Action.Play -> doPlay(it.tapeId)
                    is Action.Erase -> doErase(it.tapeId)
                }
            }
            .subscribe()
    }

    fun stop() {
        commands.onNext(Action.Stop())
    }

    fun record(tapeId: TapeId) {
        commands.onNext(Action.Record(tapeId, currentTimeMillis()))
    }

    fun play(tapeId: TapeId) {
        commands.onNext(Action.Play(tapeId))
    }

    fun erase(tapeId: TapeId) {
        commands.onNext(Action.Erase(tapeId))
    }

    private fun doRecord(tapeId: TapeId, startTime: Long): Completable =
        startRecording(tapeId)
            .flatMapCompletable { recording ->
                recordingSubject
                    .map { state ->
                        val timestamp = currentTimeMillis() - startTime
                        Sample(state, timestamp)
                    }
                    .doOnNext(recording.input)
                    .doOnDispose(recording::dispose)
                    .ignoreElements()
            }

    private fun doPlay(tapeId: TapeId): Completable =
        startPlaying(tapeId)
            .flatMapCompletable { playback ->
                Observable.fromIterable(playback.output)
                    .delay { Observable.timer(it.timestamp, TimeUnit.MILLISECONDS) }
                    .map { it.state }
                    .doOnNext(playingSubject::onNext)
                    .doOnDispose(playback::dispose)
                    .ignoreElements()
            }

    private fun doErase(tapeId: TapeId): Completable = eraseTape(tapeId)

    protected abstract fun startRecording(tapeId: TapeId): Single<Recording<State>>

    protected abstract fun startPlaying(tapeId: TapeId): Single<Playback<State>>

    protected abstract fun eraseTape(tapeId: TapeId): Completable

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Command>>): List<Interceptor<Command>> =
        interceptors + IgnoreIfPlaying()

    override fun changeResultInterceptors(interceptors: List<Interceptor<Result>>): List<Interceptor<Result>> =
        interceptors + IgnoreIfPlaying()

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> =
        interceptors + Record() + Play()

    private inner class IgnoreIfPlaying<T> : Interceptor<T> {
        override fun invoke(items: Flowable<T>): Flowable<T> =
            items.withLatestFrom(
                commands.toFlowable(BackpressureStrategy.LATEST),
                BiFunction { item: T, command: Action<TapeId> ->
                    if (command is Action.Play) Maybe.empty() else Maybe.just(item)
                })
                .flatMapMaybe { it }
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
