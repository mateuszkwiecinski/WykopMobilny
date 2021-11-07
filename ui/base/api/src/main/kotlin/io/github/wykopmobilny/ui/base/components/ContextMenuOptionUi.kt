package io.github.wykopmobilny.ui.base.components

data class ContextMenuOptionUi<T : Enum<T>>(
    val option: T,
    val onClick: () -> Unit,
)
