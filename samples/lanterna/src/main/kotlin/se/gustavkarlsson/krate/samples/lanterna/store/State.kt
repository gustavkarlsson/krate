package se.gustavkarlsson.krate.samples.lanterna.store

import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo
import se.gustavkarlsson.krate.samples.lanterna.api.models.RepoDetails

data class State(
    val repos: List<Repo> = emptyList(),
    val errors: List<String> = emptyList(),
    val isLoadingNewRepos: Boolean = false,
    val isLoadingRepoDetails: Boolean = false,
    val openRepo: RepoDetails? = null
)
