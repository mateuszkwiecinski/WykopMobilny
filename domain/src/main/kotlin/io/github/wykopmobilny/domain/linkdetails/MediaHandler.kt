package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.domain.settings.prefs.MediaPlayerPreferences

internal suspend fun openMedia(
    embed: Embed,
    preferences: MediaPlayerPreferences,
) {
    when(embed.type) {
        EmbedType.StaticImage -> TODO()
        EmbedType.AnimatedImage -> TODO()
        EmbedType.Video -> TODO()
        EmbedType.Unknown -> TODO()
    }
}
