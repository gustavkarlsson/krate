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
import se.gustavkarlsson.krate.core.Note
import se.gustavkarlsson.krate.core.NoteCreated
import se.gustavkarlsson.krate.core.NotesCommand
import se.gustavkarlsson.krate.core.NotesResult
import se.gustavkarlsson.krate.core.NotesState
import se.gustavkarlsson.krate.core.Reducer
import se.gustavkarlsson.krate.core.StateDelegate
import se.gustavkarlsson.krate.core.Transformer
import se.gustavkarlsson.krate.core.Watcher

class StoreBuilderTest {

    private val initialState = NotesState()
    private val stateAfterPlugin = NotesState(listOf(Note("plugin added me")))
    private val stateDelegate = StateDelegate<NotesState>()

    private val mockTransformer = mock<Transformer<NotesCommand, NotesResult>>()
    private val mockTypedTransformer = mock<Transformer<CreateNote, NotesResult>>()
    private val mockReducer = mock<Reducer<NotesState, NotesResult>>()
    private val mockCommandInterceptor = mock<Interceptor<NotesCommand>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }
    private val mockResultInterceptor = mock<Interceptor<NotesResult>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }
    private val mockStateInterceptor = mock<Interceptor<NotesState>> {
        on(it.invoke(any())).thenAnswer { invocation ->
            invocation.arguments[0]
        }
    }
    private val mockCommandWatcher = mock<Watcher<NotesCommand>>()
    private val mockTypedCommandWatcher = mock<Watcher<CreateNote>>()
    private val mockResultWatcher = mock<Watcher<NotesResult>>()
    private val mockTypedResultWatcher = mock<Watcher<NoteCreated>>()
    private val mockStateWatcher = mock<Watcher<NotesState>>()
    private val mockObserveScheduler = mock<Scheduler>()
    private val mockObserveSchedulerAfterPlugin = mock<Scheduler>()
    @Suppress("UNCHECKED_CAST")
    private val mockPlugin = mock<StorePlugin<NotesState, NotesCommand, NotesResult>> {
        on(it.changeInitialState(any())).thenAnswer {
            stateAfterPlugin
        }
        on(it.changeTransformers(any(), any())).thenAnswer { invocation ->
            val transformers = invocation.arguments[0] as List<Transformer<NotesCommand, NotesResult>>
            transformers + mockTransformer
        }
        on(it.changeReducer(any())).thenAnswer {
            mockReducer
        }
        on(it.changeCommandInterceptors(any(), any())).thenAnswer { invocation ->
            val interceptors = invocation.arguments[0] as List<Interceptor<NotesCommand>>
            interceptors + { stream -> stream }
        }
        on(it.changeResultInterceptors(any(), any())).thenAnswer { invocation ->
            val interceptors = invocation.arguments[0] as List<Interceptor<NotesResult>>
            interceptors + { stream -> stream }
        }
        on(it.changeStateInterceptors(any())).thenAnswer { invocation ->
            val interceptors = invocation.arguments[0] as List<Interceptor<NotesState>>
            interceptors + { stream -> stream }
        }
        on(it.changeObserveScheduler(any())).thenAnswer {
            mockObserveSchedulerAfterPlugin
        }
    }

    @Test
    fun `build full includes all added objects`() {
        val store = StoreBuilder<NotesState, NotesCommand, NotesResult>(stateDelegate)
            .apply {
                states {
                    initial = initialState
                    watchAll(mockStateWatcher)
                    intercept(mockStateInterceptor)
                    observeScheduler = mockObserveScheduler
                }
                commands {
                    transformAll(mockTransformer)
                    transform(mockTypedTransformer)
                    watchAll(mockCommandWatcher)
                    intercept(mockCommandInterceptor)
                    watch(mockTypedCommandWatcher)
                }
                results {
                    reduce(mockReducer)
                    watchAll(mockResultWatcher)
                    intercept(mockResultInterceptor)
                    watch(mockTypedResultWatcher)
                }
                plugin(mockPlugin)
            }
            .build()

        store.run {
            assertAll {
                assert(currentState).isEqualTo(stateAfterPlugin)
                assert(transformers).hasSize(3)
                assert(transformers[0]).isEqualTo(mockTransformer)
                assert(transformers[2]).isEqualTo(mockTransformer)
                assert(reducer).isEqualTo(mockReducer)
                assert(commandInterceptors).hasSize(4)
                assert(resultInterceptors).hasSize(4)
                assert(stateInterceptors).hasSize(3)
                assert(observeScheduler).isEqualTo(mockObserveSchedulerAfterPlugin)
            }
        }
    }

    @Test
    fun `build minimal succeeds`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>(stateDelegate)
            .apply {
                states {
                    initial = initialState
                }
                results {
                    reduce { state, _ -> state }
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without initial state throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>(stateDelegate)
            .apply {
                results {
                    reduce { state, _ -> state }
                }
            }

        builder.build()
    }

    @Test(expected = IllegalStateException::class)
    fun `build without reducer throws exception`() {
        val builder = StoreBuilder<NotesState, NotesCommand, NotesResult>(stateDelegate)
            .apply {
                states {
                    initial = initialState
                }
            }

        builder.build()
    }

    @Test
    fun `using state delegate`() {
        TODO()
    }
}
