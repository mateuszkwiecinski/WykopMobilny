package io.github.wykopmobilny.domain.navigation

interface YoutubeAppDetector {

    suspend fun getInstalledYoutubeApps(): Set<YoutubeApp>
}

enum class YoutubeApp {
    Official,
    Vanced,
}
