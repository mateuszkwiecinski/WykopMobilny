package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.navigation.AppGateway
import io.github.wykopmobilny.domain.navigation.YoutubeApp
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetMediaPreferences @Inject constructor(
    private val appStorage: AppStorage,
    private val appGateway: AppGateway,
) {

    operator fun invoke() = combine(
        canUseYoutubePlayer(),
        appStorage.get(UserSettings.useEmbeddedPlayer),
    ) { useYoutubePlayer, useEmbeddedPlayer ->
        MediaPlayerPreferences(
            useYoutubePlayer = useYoutubePlayer,
            useEmbeddedPlayer = useEmbeddedPlayer ?: true,
        )
    }

    private fun canUseYoutubePlayer() = appStorage.get(UserSettings.useYoutubePlayer)
        .flatMapLatest { savedValue ->
            if (savedValue == null) {
                appGateway.getInstalledYoutubeApps()
                    .map { apps -> apps.singleOrNull() == YoutubeApp.Official }
            } else {
                flowOf(savedValue)
            }
        }
}

internal data class MediaPlayerPreferences(
    val useYoutubePlayer: Boolean,
    val useEmbeddedPlayer: Boolean,
)
