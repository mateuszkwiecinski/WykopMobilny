package io.github.wykopmobilny.ui.components.widgets

data class EmbedMediaUi(
    val content: Content,
    val size: String?,
    val hasNsfwOverlay: Boolean,
    val clickAction: () -> Unit,
) {

    sealed class Content {

        data class StaticImage(
            val url: String,
            val fileName: String?,
        ) : Content()

        data class PlayableMedia(
            val url: String,
            val previewImage: String,
            val domain: String,
        ) : Content()
    }
}

const val NSFW_PLACEHOLDER = "https://www.wykop.pl/cdn/c2526412/nsfw.jpg"
