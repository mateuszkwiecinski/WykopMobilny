package io.github.wykopmobilny.ui.base.components

data class ContextMenuOptionUi(
    val label: String,
    val icon: Drawable? = null,
    val onClick: () -> Unit,
)
