package se.gustavkarlsson.krate.core

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test
import se.gustavkarlsson.krate.core.dsl.buildStore

class BuildStoreTest {

    @Test
    fun `applies block when run`() {
        val store = buildStore<Int, Unit, Unit> {
            states { initial = 5 }
            commands { transformAll { it } }
            results { reduceAll { state, _ -> state } }
        }

        assert(store.currentState).isEqualTo(5)
    }
}
