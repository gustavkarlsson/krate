package se.gustavkarlsson.krate.samples.lanterna.api.models

data class Repo(val id: Long, val name: String, val description: String?, val owner: Owner)
