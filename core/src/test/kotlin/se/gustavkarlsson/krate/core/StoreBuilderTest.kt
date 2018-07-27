package se.gustavkarlsson.krate.core

import Reducer
import Transformer
import Watcher
import assertk.assert
import assertk.assertAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Scheduler
import org.junit.Test

class StoreBuilderTest {

    private val mockState = mock<NotesState>()
    private val mockTransformer = mock<Transformer<NotesState, NotesCommand, NotesResult>>()
    private val mockTypedTransformer = mock<Transformer<NotesState, CreateNote, NotesResult>>()
    private val mockReducer = mock<Reducer<NotesState, NotesResult>>()
    private val mockTypedReducer = mock<Reducer<NotesState, NoteCreated>>()
    private val mockCommandWatcher = mock<Watcher<NotesCommand>>()
    private val mockTypedCommandWatcher = mock<Watcher<CreateNote>>()
    private val mockResultWatcher = mock<Watcher<NotesResult>>()
    private val mockTypedResultWatcher = mock<Watcher<NoteCreated>>()
    private val mockStateWatcher = mock<Watcher<NotesState>>()
    private val mockErrorWatcher = mock<Watcher<Throwable>>()
    private val mockObserveScheduler = mock<Scheduler>()

    @Test
    fun `build full includes all added objects`() {
        val store = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                setInitialState(mockState)
                transform(mockTransformer)
                transformByType(mockTypedTransformer)
                reduce(mockReducer)
                reduceByType(mockTypedReducer)
                watchCommands(mockCommandWatcher)
                watchCommandsByType(mockTypedCommandWatcher)
                watchResults(mockResultWatcher)
                watchResultsByType(mockTypedResultWatcher)
                watchStates(mockStateWatcher)
                watchErrors(mockErrorWatcher)
                observeOn(mockObserveScheduler)
                retryOnError(true)
            }
            .build()

        store.run {
            assertAll {
                assert(currentState).isEqualTo(mockState)
                assert(transformers).hasSize(2)
                assert(transformers[0]).isEqualTo(mockTransformer)
                assert(reducers).hasSize(2)
                assert(reducers[0]).isEqualTo(mockReducer)
                assert(commandWatchers).hasSize(2)
                assert(commandWatchers[0]).isEqualTo(mockCommandWatcher)
                assert(resultWatchers).hasSize(2)
                assert(resultWatchers[0]).isEqualTo(mockResultWatcher)
                assert(stateWatchers).containsExactly(mockStateWatcher)
                assert(errorWatchers).containsExactly(mockErrorWatcher)
                assert(observeScheduler).isEqualTo(mockObserveScheduler)
                assert(retryOnError).isTrue()
            }
        }
    }

    @Test
    fun `build minimal succeeds`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                setInitialState(mockState)
                transform(mockTransformer)
                reduce(mockReducer)
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without initial state throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                transform(mockTransformer)
                reduce(mockReducer)
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without transformer throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                setInitialState(mockState)
                reduce(mockReducer)
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without reducer throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                setInitialState(mockState)
                transform(mockTransformer)
            }

        builder.build()
    }
}
