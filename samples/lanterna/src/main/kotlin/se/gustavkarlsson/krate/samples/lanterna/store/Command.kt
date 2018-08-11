package se.gustavkarlsson.krate.samples.lanterna.store

sealed class Command {
    object LoadMoreRepos : Command()
    data class LoadRepoDetails(val index: Int) : Command()
    object CloseRepoDetails : Command()
    data class AcknowledgeError(val error: String) : Command()
}
