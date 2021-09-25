package io.github.wykopmobilny.domain.di

interface HasScopeInitializer {

    fun initializer(): ScopeInitializer
}

interface ScopeInitializer {

    suspend fun initialize()
}
