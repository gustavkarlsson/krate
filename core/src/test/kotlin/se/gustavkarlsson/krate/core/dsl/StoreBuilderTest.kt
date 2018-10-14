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

    private val mockInitialState = mock<NotesState>()
    private val mockStateAfterPlugin = mock<NotesState>()
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
    private val mockObserveSchedulerAfterPlugin = mock<Scheduler>()
    private val mockPlugin = mock<StorePlugin<NotesState, NotesCommand, NotesResult>> {
        on(it.changeInitialState(any())).thenAnswer {
            mockStateAfterPlugin
        }
        on(it.changeTransformers(any())).thenAnswer {
            val transformers = it.arguments[0] as List<StateAwareTransformer<NotesState, NotesCommand, NotesResult>>
            transformers + mockTransformer
        }
        on(it.changeReducers(any())).thenAnswer {
            val reducers = it.arguments[0] as List<Reducer<NotesState, NotesResult>>
            reducers + mockReducer
        }
        on(it.changeCommandInterceptors(any())).thenAnswer {
            val interceptors = it.arguments[0] as List<Interceptor<NotesCommand>>
            interceptors + { it }
        }
        on(it.changeResultInterceptors(any())).thenAnswer {
            val interceptors = it.arguments[0] as List<Interceptor<NotesResult>>
            interceptors + { it }
        }
        on(it.changeStateInterceptors(any())).thenAnswer {
            val interceptors = it.arguments[0] as List<Interceptor<NotesState>>
            interceptors + { it }
        }
        on(it.changeObserveScheduler(any())).thenAnswer {
            mockObserveSchedulerAfterPlugin
        }
    }

    @Test
    fun `build full includes all added objects`() {
        val store = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockInitialState
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
                plugin(mockPlugin)
            }
            .build()

        store.run {
            assertAll {
                assert(currentState).isEqualTo(mockStateAfterPlugin)
                assert(transformers).hasSize(3)
                assert(transformers[0]).isEqualTo(mockTransformer)
                assert(transformers[2]).isEqualTo(mockTransformer)
                assert(reducers).hasSize(3)
                assert(reducers[0]).isEqualTo(mockReducer)
                assert(reducers[2]).isEqualTo(mockReducer)
                assert(commandInterceptors).hasSize(4)
                assert(resultInterceptors).hasSize(4)
                assert(stateInterceptors).hasSize(3)
                assert(observeScheduler).isEqualTo(mockObserveSchedulerAfterPlugin)
            }
        }
    }

    @Test
    fun `build minimal succeeds`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockInitialState
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without initial state throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>()

        builder.build()
    }
}
