package se.gustavkarlsson.krate.samples.lanterna.store

import io.reactivex.Flowable
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.lanterna.api.github
import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo

val store = buildStore<State, Command> { getState ->

    commands {
        transform<Command.LoadMoreRepos> {
            flatMap {
                val state = getState()
                if (state.isLoadingNewRepos)
                    Flowable.empty()
                else
                    loadMoreRepos(state.repos.lastOrNull()?.id)
            }
        }

        transform<Command.LoadRepoDetails> {
            switchMap {
                loadRepoDetails(getState().repos[it.index])
            }
        }
    }

    states {
        initial = State()

        reduce<Command.LoadingMoreRepos> {
            copy(isLoadingNewRepos = it.isLoading)
        }

        reduce<Command.LoadingRepoDetails> {
            copy(isLoadingRepoDetails = it.isLoading)
        }

        reduce<Command.LoadedMoreRepos> {
            copy(repos = repos + it.repos)
        }

        reduce<Command.LoadedRepoDetails> {
            copy(openRepo = it.repo)
        }

        reduce<Command.CloseRepoDetails> {
            copy(openRepo = null)
        }

        reduce<Command.AddError> {
            copy(errors = errors + it.error)
        }

        reduce<Command.RemoveError> { result ->
            copy(errors = errors.filter { it != result.error })
        }
    }
}

private fun loadRepoDetails(repo: Repo): Flowable<Command> {
    return github.getRepo(repo.owner.login, repo.name)
        .flatMapPublisher {
            Flowable.just(
                Command.LoadingRepoDetails(false),
                Command.LoadedRepoDetails(it)
            )
        }
        .onErrorResumeNext { throwable: Throwable ->
            val error = throwable.message ?: "Failed to load repo: ${repo.name}"
            Flowable.just(
                Command.LoadingRepoDetails(false),
                Command.AddError(error)
            )
        }
        .startWith(Command.LoadingRepoDetails(true))
}

private fun loadMoreRepos(lastId: Long?): Flowable<Command> {
    return github.getRepos(lastId)
        .flatMapPublisher {
            Flowable.just(
                Command.LoadingMoreRepos(false),
                Command.LoadedMoreRepos(it)
            )
        }
        .onErrorResumeNext { throwable: Throwable ->
            val error = throwable.message ?: "Failed to load repos"
            Flowable.just(
                Command.LoadingMoreRepos(false),
                Command.AddError(error)
            )
        }
        .startWith(Command.LoadingMoreRepos(true))
}
