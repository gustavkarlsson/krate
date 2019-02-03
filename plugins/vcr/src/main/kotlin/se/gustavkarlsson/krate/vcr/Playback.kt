package se.gustavkarlsson.krate.vcr

import io.reactivex.disposables.Disposable

interface Playback<State : Any> : Disposable {
    val output: Iterable<Sample<State>>
}
