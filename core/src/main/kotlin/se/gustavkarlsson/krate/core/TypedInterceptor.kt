package se.gustavkarlsson.krate.core

import io.reactivex.Flowable

/**
 * A transformer that filters commands of a specified type and applies another transformer for that type to it.
 */
class TypedInterceptor<TUpper : Any, T : TUpper>(
    private val type: Class<T>,
    private val transform: Transformer<T, TUpper>
) : Interceptor<TUpper> {

    override fun invoke(commands: Flowable<TUpper>): Flowable<TUpper> {
        return commands.publish { published ->
            val ignored = published.filter { !type.isInstance(it) }
            val transformed = transform(published.ofType(type))
            Flowable.merge(ignored, transformed)
        }
    }
}
