package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.domain.navigation.YoutubeApp
import io.github.wykopmobilny.domain.navigation.YoutubeAppDetector
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.storage.api.UserPreferenceApi
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetMediaPreferences @Inject constructor(
    private val userPreferences: UserPreferenceApi,
    private val youtubeAppDetector: YoutubeAppDetector,
) {

    operator fun invoke() = combine(
        userPreferences.get(UserSettings.useYoutubePlayer),
        userPreferences.get(UserSettings.useEmbeddedPlayer),
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
