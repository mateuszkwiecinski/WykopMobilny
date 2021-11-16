package io.github.wykopmobilny.domain.profile.datasource

import io.github.wykopmobilny.api.responses.EmbedResponse
import io.github.wykopmobilny.api.responses.properUrl
import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType

internal fun EmbedResponse.toEntity(): Embed {
    val knownType = when (type) {
        "image" -> if (animated) {
            EmbedType.AnimatedImage
        } else {
            EmbedType.StaticImage
        }
        "video" -> EmbedType.Video
        else -> EmbedType.Unknown
    }

    return Embed(
        id = properUrl,
        type = knownType,
        fileName = source,
        preview = preview,
        size = size,
        hasAdultContent = plus18,
        ratio = ratio,
    )
}
