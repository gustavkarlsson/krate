package se.gustavkarlsson.krate.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class StateDelegate<State : Any>(
    @Volatile internal var value: State? = null
) : ReadWriteProperty<Any, State> {

    internal val valueUnsafe: State get() = checkNotNull(value) { "state has not been set" }

    override fun getValue(thisRef: Any, property: KProperty<*>): State = valueUnsafe

    override fun setValue(thisRef: Any, property: KProperty<*>, value: State) { this.value = value }
}
