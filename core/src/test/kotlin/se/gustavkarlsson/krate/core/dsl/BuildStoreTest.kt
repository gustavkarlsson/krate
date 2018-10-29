package se.gustavkarlsson.krate.core.dsl

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test

class BuildStoreTest {

    @Test
    fun `applies block when run`() {
        val store = buildStore<Int, Unit, Unit> {
            states { initial = 5 }
            commands { transformAll { it } }
            results { reduce { state, _ -> state } }
        }

        assert(store.currentState).isEqualTo(5)
    }
}
