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
            results { reduceAll { state, _ -> state } }
        }

        assert(store.currentState).isEqualTo(5)
    }

    @Test
    fun `getState gets the current state2`() {
        val store = buildStore<List<Int>, Int, List<Int>> { getState ->
            states {
                initial = listOf(0)
                initial = getState() + 1
            }
            commands { transformAll { commands -> commands.map { getState() + it } } }
            results { reduceAll { _, result -> result } }
        }
        store.issue(2)

        assert(store.currentState).isEqualTo(listOf(0, 1, 2))
    }
}
