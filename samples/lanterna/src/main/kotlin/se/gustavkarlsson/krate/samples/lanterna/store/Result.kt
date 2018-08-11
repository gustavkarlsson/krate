package se.gustavkarlsson.krate.samples.lanterna.store

import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo
import se.gustavkarlsson.krate.samples.lanterna.api.models.RepoDetails

sealed class Result {
    data class LoadingMoreRepos(val isLoading: Boolean) : Result()
    data class LoadingRepoDetails(val isLoading: Boolean) : Result()
    data class LoadedMoreRepos(val repos: List<Repo>) : Result()
    data class LoadedRepoDetails(val repo: RepoDetails) : Result()
    object ClosedRepoDetails : Result()
    data class GotError(val error: String) : Result()
    data class RemovedError(val error: String) : Result()
}
