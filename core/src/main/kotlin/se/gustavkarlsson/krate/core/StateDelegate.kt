package se.gustavkarlsson.krate.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class StateDelegate<T : Any>(
    @Volatile internal var value: T? = null
) : ReadWriteProperty<Any, T> {

    internal val valueUnsafe: T get() = requireNotNull(value) { "state has not been set" }

    override fun getValue(thisRef: Any, property: KProperty<*>): T = valueUnsafe

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) { this.value = value }
}
