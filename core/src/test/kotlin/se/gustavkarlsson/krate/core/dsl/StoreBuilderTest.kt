package se.gustavkarlsson.krate.core.dsl

import Reducer
import StateAwareTransformer
import Interceptor
import assertk.assert
import assertk.assertAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.nhaarman.mockitokotlin2.any
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
    private val mockObserveScheduler = mock<Scheduler>()

    @Test
    fun `build full includes all added objects`() {
        val store = StoreBuilder<NotesState, NotesCommand, NotesResult>()
            .apply {
                states {
                    initial = mockState
                    intercept(mockStateInterceptor)
                    observeScheduler = mockObserveScheduler
                }
                commands {
                    transformAllWithState(mockTransformer)
                    transformWithState(mockTypedTransformer)
                    intercept(mockCommandInterceptor)
                }
                results {
                    reduceAll(mockReducer)
                    reduce(mockTypedReducer)
                    intercept(mockResultInterceptor)
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
                assert(commandInterceptors).containsExactly(mockCommandInterceptor)
                assert(resultInterceptors).containsExactly(mockResultInterceptor)
                assert(stateInterceptors).containsExactly(mockStateInterceptor)
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
