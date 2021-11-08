package io.github.wykopmobilny.ui.components.widgets

data class EmbedMediaUi(
    val previewUrl: String,
    val fileName: String?,
    val size: String?,
    val overlay: Overlay?,
    val clickAction: () -> Unit,
) {
    enum class Overlay {
        Nsfw,
        PlayGif,
        PlayVideo,
    }
}
