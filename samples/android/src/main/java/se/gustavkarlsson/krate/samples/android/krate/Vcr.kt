package se.gustavkarlsson.krate.samples.android.krate

import se.gustavkarlsson.krate.vcr.implementations.InMemoryVcr

val vcr = InMemoryVcr<State, Command, Result>()
