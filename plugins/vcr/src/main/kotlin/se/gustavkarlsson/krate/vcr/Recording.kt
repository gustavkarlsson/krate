package se.gustavkarlsson.krate.vcr

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

interface Recording<State : Any> : Disposable {
    val input: Consumer<Sample<State>>
}
