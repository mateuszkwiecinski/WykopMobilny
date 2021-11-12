package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import kotlinx.datetime.Instant
import java.net.URL

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
    content = when (type) {
        EmbedType.AnimatedImage ->
            EmbedMediaUi.Content.PlayableMedia(
                url = id,
                previewImage = preview,
                domain = "Gif",
            )
        EmbedType.Video ->
            EmbedMediaUi.Content.PlayableMedia(
                url = id,
                previewImage = preview,
                domain = URL(id).host.split(".").dropLast(1).lastOrNull() ?: URL(id).host,
            )
        EmbedType.StaticImage,
        EmbedType.Unknown,
        -> EmbedMediaUi.Content.StaticImage(
            url = if (useLowQualityImage) {
                id
            } else {
                preview
            },
            fileName = fileName,
        )
    },
    size = size.takeIf { useLowQualityImage || type == EmbedType.AnimatedImage },
    hasNsfwOverlay = hasNsfwOverlay,
    clickAction = clickAction,
)
