package io.github.wykopmobilny.ui.components.widgets

data class ColoredCounterUi(
    val count: Int,
    val color: Color,
    val onClick: (() -> Unit)?,
)

data class PlainCounterUi(
    val count: Int,
    val onClick: (() -> Unit)?,
)

data class TwoActionsCounterUi(
    val count: Int,
    val onUpvote: () -> Unit,
    val onDownvote: () -> Unit,
)
