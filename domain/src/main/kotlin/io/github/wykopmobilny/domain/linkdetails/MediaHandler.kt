package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.settings.prefs.MediaPlayerPreferences
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal suspend fun InteropRequestsProvider.openMedia(embed: Embed, preferences: MediaPlayerPreferences, onUnknown: suspend () -> Unit) =
    withContext(AppDispatchers.Default) {
        when (embed.type) {
            EmbedType.StaticImage -> request(InteropRequest.ShowImage(embed.id))
            EmbedType.AnimatedImage -> request(InteropRequest.ShowGif(embed.id))
            EmbedType.Video -> when (URL(embed.id).host.removePrefix("www.")) {
                "youtu.be", "youtube.com" ->
                    if (preferences.useYoutubePlayer) {
                        request(InteropRequest.OpenYoutube(embed.id))
                    } else {
                        request(InteropRequest.WebBrowser(embed.id))
                    }
                else ->
                    if (preferences.useEmbeddedPlayer) {
                        request(InteropRequest.OpenPlayer(embed.id))
                    } else {
                        request(InteropRequest.WebBrowser(embed.id))
                    }
            }
            EmbedType.Unknown -> onUnknown()
        }
    }
