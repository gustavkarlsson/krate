package se.gustavkarlsson.krate.samples.lanterna.api.models

import com.squareup.moshi.Json

data class RepoDetails(
    val id: Long,
    val name: String,
    val description: String?,
    val owner: Owner,
    @Json(name = "stargazers_count") val stars: Int,
    val license: License?
)
