package se.gustavkarlsson.krate.core

internal fun <T : Any, R : Any> Any?.mapIfInstanceOf(
    type: Class<T>,
    map: (T) -> R
): R? {
    return if (type.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        map(this as T)
    } else {
        null
    }
}
