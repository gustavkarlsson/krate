package se.gustavkarlsson.krate.core

import kotlin.reflect.KClass

internal fun <T : Any, R : Any> Any?.ifObjectInstanceOf(
    type: KClass<T>,
    handle: (T) -> R
): R? {
    return if (type.javaObjectType.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        handle(this as T)
    } else {
        null
    }
}
