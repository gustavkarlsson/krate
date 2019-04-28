package se.gustavkarlsson.krate.core

/**
 * A watcher that filters results of a specified type and applies another watcher for that type to it.
 */
class TypedWatcher<Type : Any, T : Type>(
    private val type: Class<T>,
    private val watch: Watcher<T>
) : Watcher<Type> {

    override fun invoke(value: Type) {
        value.ifObjectInstanceOf(type, watch)
    }
}
