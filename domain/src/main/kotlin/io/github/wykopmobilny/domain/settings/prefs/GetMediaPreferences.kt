package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.navigation.YoutubeApp
import io.github.wykopmobilny.domain.navigation.YoutubeAppDetector
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetMediaPreferences @Inject constructor(
    private val appStorage: AppStorage,
    private val youtubeAppDetector: YoutubeAppDetector,
) {

    operator fun invoke() = combine(
        appStorage.get(UserSettings.useYoutubePlayer),
        appStorage.get(UserSettings.useEmbeddedPlayer),
    ) { useYoutubePlayer, useEmbeddedPlayer ->
        MediaPlayerPreferences(
            useYoutubePlayer = useYoutubePlayer ?: canUseYoutubePlayer(),
            useEmbeddedPlayer = useEmbeddedPlayer ?: true,
        )
    }

    private suspend fun canUseYoutubePlayer(): Boolean {
        val installed = youtubeAppDetector.getInstalledYoutubeApps()

        return installed.contains(YoutubeApp.Official)
    }
}

data class MediaPlayerPreferences(
    val useYoutubePlayer: Boolean,
    val useEmbeddedPlayer: Boolean,
)
