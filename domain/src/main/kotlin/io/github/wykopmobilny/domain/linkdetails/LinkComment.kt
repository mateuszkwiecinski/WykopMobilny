package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi.Overlay
import kotlinx.datetime.Instant

internal data class LinkComment(
    val id: Long,
    val app: String?,
    val body: String,
    val postedAt: Instant,
    val author: UserInfo,
    val plusCount: Int,
    val minusCount: Int,
    val userAction: UserVote?,
    val embed: Embed?,
) {

    val totalCount = plusCount - minusCount
}

internal fun LinkComment.wykopUrl(linkId: Long) =
    "https://www.wykop.pl/link/$linkId/#comment-$id"

internal fun Embed.toUi(
    useLowQualityImage: Boolean,
    hasNsfwOverlay: Boolean,
    clickAction: () -> Unit,
) = EmbedMediaUi(
    previewUrl = if (useLowQualityImage) {
        preview
    } else {
        id
    },
    fileName = fileName,
    size = size,
    clickAction = clickAction,
    overlay = if (hasNsfwOverlay) {
        Overlay.Nsfw
    } else {
        when (type) {
            EmbedType.AnimatedImage -> Overlay.PlayGif
            EmbedType.Video -> Overlay.PlayVideo
            EmbedType.StaticImage,
            EmbedType.Unknown,
            -> null
        }
    },
)
