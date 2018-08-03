package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test

class BuildStoreTest {

    @Test
    fun `applies block when run`() {
        val store = buildStore<Int, Unit, Unit> {
            setInitialState(5)
            transform { commands, _ -> commands }
            reduce { state, _ -> state }
        }

        assert(store.currentState).isEqualTo(5)
    }
}
