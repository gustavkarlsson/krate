package se.gustavkarlsson.krate.vcr

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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
    private val commands = PublishSubject.create<Command<TapeId>>().toSerialized()
    private val playingSubject = PublishSubject.create<State>()
    private val recordingSubject = BehaviorSubject.create<State>()

    init {
        commands
            .switchMapCompletable {
                when (it) {
                    is Command.Stop -> Completable.complete()
                    is Command.Record -> doRecord(it.tapeId, it.startTime)
                    is Command.Play -> doPlay(it.tapeId)
                    is Command.Erase -> doErase(it.tapeId)
                }
            }
            .subscribe()
    }

    fun record(tapeId: TapeId) {
        commands.onNext(Command.Record(tapeId, currentTimeMillis()))
    }

    fun play(tapeId: TapeId) {
        commands.onNext(Command.Play(tapeId))
    }

    fun erase(tapeId: TapeId) {
        commands.onNext(Command.Erase(tapeId))
    }

    private fun doRecord(tapeId: TapeId, startTime: Long): Completable =
        startRecording(tapeId)
            .flatMapCompletable { recording ->
                recordingSubject
                    .doOnNext { state ->
                        val timestamp = currentTimeMillis() - startTime
                        val sample = Sample(state, timestamp)
                        recording.write(sample)
                    }
                    .doOnDispose(recording::dispose)
                    .ignoreElements()
            }

    private fun doPlay(tapeId: TapeId): Completable =
        startPlaying(tapeId)
            .delay { Flowable.timer(it.timestamp, TimeUnit.MILLISECONDS) }
            .map { it.state }
            .doOnNext(playingSubject::onNext)
            .ignoreElements()

    private fun doErase(tapeId: TapeId): Completable = eraseTape(tapeId)

    protected abstract fun startRecording(tapeId: TapeId): Single<Recording<State>>

    protected abstract fun startPlaying(tapeId: TapeId): Flowable<Sample<State>>

    protected abstract fun eraseTape(tapeId: TapeId): Completable

    override fun changeCommandInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeResultInterceptors(interceptors: List<Interceptor<Any>>): List<Interceptor<Any>> =
        interceptors + IgnoreIfPlaying()

    override fun changeStateInterceptors(interceptors: List<Interceptor<State>>): List<Interceptor<State>> =
        interceptors + Record() + Play()

    private inner class IgnoreIfPlaying<T> : Interceptor<T> {
        override fun invoke(items: Flowable<T>): Flowable<T> =
            items.withLatestFrom(
                commands.toFlowable(BackpressureStrategy.LATEST),
                BiFunction { item: T, command: Command<TapeId> ->
                    if (command is Command.Play) Maybe.empty() else Maybe.just(item)
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
