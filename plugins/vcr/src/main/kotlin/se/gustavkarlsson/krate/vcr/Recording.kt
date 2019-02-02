package se.gustavkarlsson.krate.vcr

import io.reactivex.disposables.Disposable

interface Recording<State : Any> : Disposable {
    fun write(sample: Sample<State>)
}
