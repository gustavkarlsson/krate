package se.gustavkarlsson.krate.samples.lanterna.store

import io.reactivex.Observable
import se.gustavkarlsson.krate.core.buildStore
import se.gustavkarlsson.krate.samples.lanterna.api.github

val store = buildStore<State, Command, Result> {

    setInitialState(State())

    transformByType<Command.LoadMoreRepos> { getState ->
        flatMap {
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

    transformByType<Command.LoadRepoDetails> { getState ->
        switchMap {
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

    transformByType<Command.CloseRepoDetails> {
        map { Result.ClosedRepoDetails }
    }

    transformByType<Command.AcknowledgeError> {
        map { Result.RemovedError(it.error) }
    }

    reduceByType<Result.LoadingMoreRepos> { state, result ->
        state.copy(isLoadingNewRepos = result.isLoading)
    }

    reduceByType<Result.LoadingRepoDetails> { state, result ->
        state.copy(isLoadingRepoDetails = result.isLoading)
    }

    reduceByType<Result.LoadedMoreRepos> { state, result ->
        state.copy(repos = state.repos + result.repos)
    }

    reduceByType<Result.LoadedRepoDetails> { state, result ->
        state.copy(shownRepo = result.repo)
    }

    reduceByType<Result.ClosedRepoDetails> { state, _ ->
        state.copy(shownRepo = null)
    }

    reduceByType<Result.GotError> { state, result ->
        state.copy(errors = state.errors + result.error)
    }

    reduceByType<Result.RemovedError> { state, result ->
        state.copy(errors = state.errors.filter { it != result.error })
    }
}
