package se.gustavkarlsson.krate.core.dsl

import Reducer
import StatefulTransformer
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
import se.gustavkarlsson.krate.core.CreateNote
import se.gustavkarlsson.krate.core.NoteCreated
import se.gustavkarlsson.krate.core.NotesCommand
import se.gustavkarlsson.krate.core.NotesResult
import se.gustavkarlsson.krate.core.NotesState

class StoreBuilderTest {

    private val mockState = mock<NotesState>()
    private val mockTransformer = mock<StatefulTransformer<NotesState, NotesCommand, NotesResult>>()
    private val mockTypedTransformer = mock<StatefulTransformer<NotesState, CreateNote, NotesResult>>()
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
                states {
                    initial = mockState
                    watch(mockStateWatcher)
                    observeScheduler = mockObserveScheduler
                }
                commands {
                    transformAllWithState(mockTransformer)
                    transformWithState(mockTypedTransformer)
                    watchAll(mockCommandWatcher)
                    watch(mockTypedCommandWatcher)
                }
                results {
                    reduceAll(mockReducer)
                    reduce(mockTypedReducer)
                    watchAll(mockResultWatcher)
                    watch(mockTypedResultWatcher)
                }
                errors {
                    retry = true
                    watch(mockErrorWatcher)
                }
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
                states {
                    initial = mockState
                }
                commands {
                    transformAllWithState(mockTransformer)
                }
                results {
                    reduceAll(mockReducer)
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without initial state throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                commands {
                    transformAllWithState(mockTransformer)
                }
                results {
                    reduceAll(mockReducer)
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without transformer throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockState
                }
                results {
                    reduceAll(mockReducer)
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without reducer throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockState
                }
                commands {
                    transformAllWithState(mockTransformer)
                }
            }

        builder.build()
    }
}
