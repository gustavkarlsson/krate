package se.gustavkarlsson.krate.samples.lanterna.store

import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo
import se.gustavkarlsson.krate.samples.lanterna.api.models.RepoDetails

sealed class Command {
    object LoadMoreRepos : Command()
    data class LoadingMoreRepos(val isLoading: Boolean) : Command()
    data class LoadedMoreRepos(val repos: List<Repo>) : Command()

    data class LoadRepoDetails(val index: Int) : Command()
    data class LoadingRepoDetails(val isLoading: Boolean) : Command()
    data class LoadedRepoDetails(val repo: RepoDetails) : Command()

    data class AddError(val error: String) : Command()
    data class RemoveError(val error: String) : Command()
    object CloseRepoDetails : Command()
}
