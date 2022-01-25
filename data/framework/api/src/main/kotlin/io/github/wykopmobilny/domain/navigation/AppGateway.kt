package io.github.wykopmobilny.domain.navigation

import kotlinx.coroutines.flow.Flow

interface AppGateway {

    fun getInstalledYoutubeApps(): Flow<Set<YoutubeApp>>

    fun getInstalledAuthenticatorApps(): Flow<Set<AuthenticatorApp>>

    suspend fun openApp(app: ExternalApp)
}

sealed interface ExternalApp

enum class YoutubeApp : ExternalApp {
    Official,
    Vanced,
}

enum class AuthenticatorApp : ExternalApp {
    Google,
    Microsoft,
    Authy,
}
