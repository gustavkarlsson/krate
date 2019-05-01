package se.gustavkarlsson.krate.core

/**
 * A watcher that filters elements of a specified type and applies the given watcher for that type to it.
 */
class TypedWatcher<TUpper : Any, T : TUpper>(
    private val type: Class<T>,
    private val watch: Watcher<T>
) : Watcher<TUpper> {

    override fun invoke(value: TUpper) {
        value.mapIfInstanceOf(type, watch)
    }
}
