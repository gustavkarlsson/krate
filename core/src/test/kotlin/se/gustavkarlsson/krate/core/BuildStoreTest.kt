package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildStoreTest {

    @Test
    fun `applies block when run`() {
        val store = buildStore<Int, Unit, Unit> {
            setInitialState(5)
            transform { this }
            reduce { state, _ -> state }
        }

        assert(store.currentState).isEqualTo(5)
    }
}
