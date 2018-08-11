package se.gustavkarlsson.krate.samples.lanterna.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import se.gustavkarlsson.krate.samples.lanterna.api.models.Repo
import se.gustavkarlsson.krate.samples.lanterna.api.models.RepoDetails

interface GithubApi {

    @GET("/repositories")
    fun getRepos(@Query("since") sinceId: Long? = null): Single<List<Repo>>

    @GET("/repos/{owner}/{name}")
    fun getRepo(@Path("owner") owner: String, @Path("name") name: String): Single<RepoDetails>
}
