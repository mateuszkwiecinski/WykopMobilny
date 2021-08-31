package io.github.wykopmobilny.storage.api

data class Blacklist(
    val tags: Set<String>,
    val users: Set<String>,
) {

    companion object {

        fun empty() = Blacklist(
            tags = emptySet(),
            users = emptySet(),
        )
    }
}
