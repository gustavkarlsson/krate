package se.gustavkarlsson.krate.samples.lanterna.store

import io.reactivex.Observable
import se.gustavkarlsson.krate.core.dsl.buildStore
import se.gustavkarlsson.krate.samples.lanterna.api.github

val store = buildStore<State, Command, Result> {

    states {
        initial = State()
    }

    commands {
        transformWithState<Command.LoadMoreRepos> { commands, getState ->
            commands.flatMap {
                val state = getState()
                if (state.isLoadingNewRepos) {
                    Observable.empty<Result>()
                } else {
                    val lastId = state.repos.lastOrNull()?.id
                    github.getRepos(lastId)
                        .flatMapObservable {
                            Observable.just(Result.LoadingMoreRepos(false), Result.LoadedMoreRepos(it))
                        }
                        .onErrorResumeNext { throwable: Throwable ->
                            val error = throwable.message ?: "Failed to load repos"
                            Observable.just(Result.LoadingMoreRepos(false), Result.GotError(error))
                        }
                        .startWith(Result.LoadingMoreRepos(true))
                }
            }
        }

        transformWithState<Command.LoadRepoDetails> { commands, getState ->
            commands.switchMap {
                val repo = getState().repos[it.index]
                github.getRepo(repo.owner.login, repo.name)
                    .flatMapObservable {
                        Observable.just(Result.LoadingRepoDetails(false), Result.LoadedRepoDetails(it))
                    }
                    .onErrorResumeNext { throwable: Throwable ->
                        val error = throwable.message ?: "Failed to load repo: ${repo.name}"
                        Observable.just(Result.LoadingRepoDetails(false), Result.GotError(error))
                    }
                    .startWith(Result.LoadingRepoDetails(true))
            }
        }

        transform<Command.CloseRepoDetails> { commands ->
            commands.map { Result.ClosedRepoDetails }
        }

        transform<Command.AcknowledgeError> { commands ->
            commands.map { Result.RemovedError(it.error) }
        }
    }

    results {
        reduce<Result.LoadingMoreRepos> { state, result ->
            state.copy(isLoadingNewRepos = result.isLoading)
        }

        reduce<Result.LoadingRepoDetails> { state, result ->
            state.copy(isLoadingRepoDetails = result.isLoading)
        }

        reduce<Result.LoadedMoreRepos> { state, result ->
            state.copy(repos = state.repos + result.repos)
        }

        reduce<Result.LoadedRepoDetails> { state, result ->
            state.copy(openRepo = result.repo)
        }

        reduce<Result.ClosedRepoDetails> { state, _ ->
            state.copy(openRepo = null)
        }

        reduce<Result.GotError> { state, result ->
            state.copy(errors = state.errors + result.error)
        }

        reduce<Result.RemovedError> { state, result ->
            state.copy(errors = state.errors.filter { it != result.error })
        }
    }
}
