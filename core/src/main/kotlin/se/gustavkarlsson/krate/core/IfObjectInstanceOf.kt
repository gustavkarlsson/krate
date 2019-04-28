package se.gustavkarlsson.krate.core

internal fun <T : Any, R : Any> Any?.ifObjectInstanceOf(
    type: Class<T>,
    handle: (T) -> R
): R? {
    return if (type.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        handle(this as T)
    } else {
        null
    }
}
