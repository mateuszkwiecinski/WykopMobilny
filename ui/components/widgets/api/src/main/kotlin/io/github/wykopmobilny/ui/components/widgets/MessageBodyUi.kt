package io.github.wykopmobilny.ui.components.widgets

data class MessageBodyUi(
    val content: CharSequence?,
    val maxLines: Int,
    val viewMoreAction: () -> Unit,
)
