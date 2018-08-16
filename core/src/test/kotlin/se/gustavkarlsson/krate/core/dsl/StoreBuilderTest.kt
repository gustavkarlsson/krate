package se.gustavkarlsson.krate.core.dsl

import assertk.assert
import assertk.assertAll
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Scheduler
import org.junit.Test
import se.gustavkarlsson.krate.core.CreateNote
import se.gustavkarlsson.krate.core.Interceptor
import se.gustavkarlsson.krate.core.NoteCreated
import se.gustavkarlsson.krate.core.NotesCommand
import se.gustavkarlsson.krate.core.NotesResult
import se.gustavkarlsson.krate.core.NotesState
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateAwareTransformer
import se.gustavkarlsson.krate.core.Watcher

class StoreBuilderTest {

    private val mockState = mock<NotesState>()
    private val mockTransformer = mock<StateAwareTransformer<NotesState, NotesCommand, NotesResult>>()
    private val mockTypedTransformer = mock<StateAwareTransformer<NotesState, CreateNote, NotesResult>>()
    private val mockReducer = mock<Reducer<NotesState, NotesResult>>()
    private val mockTypedReducer = mock<Reducer<NotesState, NoteCreated>>()
    private val mockCommandInterceptor = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }
    private val mockResultInterceptor = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }
    private val mockStateInterceptor = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer {
            it.arguments[0]
        }
    }
    private val mockCommandWatcher = mock<Watcher<NotesCommand>>()
    private val mockTypedCommandWatcher = mock<Watcher<CreateNote>>()
    private val mockResultWatcher = mock<Watcher<NotesResult>>()
    private val mockTypedResultWatcher = mock<Watcher<NoteCreated>>()
    private val mockStateWatcher = mock<Watcher<NotesState>>()
    private val mockObserveScheduler = mock<Scheduler>()

    @Test
    fun `build full includes all added objects`() {
        val store = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockState
                    watchAll(mockStateWatcher)
                    intercept(mockStateInterceptor)
                    observeScheduler = mockObserveScheduler
                }
                commands {
                    transformAllWithState(mockTransformer)
                    transformWithState(mockTypedTransformer)
                    watchAll(mockCommandWatcher)
                    intercept(mockCommandInterceptor)
                    watch(mockTypedCommandWatcher)
                }
                results {
                    reduceAll(mockReducer)
                    reduce(mockTypedReducer)
                    watchAll(mockResultWatcher)
                    intercept(mockResultInterceptor)
                    watch(mockTypedResultWatcher)
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
                assert(commandInterceptors).hasSize(3)
                assert(resultInterceptors).hasSize(3)
                assert(stateInterceptors).hasSize(2)
                assert(observeScheduler).isEqualTo(mockObserveScheduler)
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
