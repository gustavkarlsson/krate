package se.gustavkarlsson.krate.core

import kotlin.reflect.KClass

/**
 * A watcher that filters results of a specified type and applies another watcher for that type to it.
 */
class TypedWatcher<Type : Any, T : Type>(
    private val type: KClass<T>,
    private val watch: Watcher<T>
) : Watcher<Type> {

    override fun invoke(value: Type) {
        value.ifObjectInstanceOf(type, watch)
    }
}
