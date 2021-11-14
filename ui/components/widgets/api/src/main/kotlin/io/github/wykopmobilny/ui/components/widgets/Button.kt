package io.github.wykopmobilny.ui.components.widgets

import io.github.wykopmobilny.ui.base.components.Drawable

data class Button(
    val label: String,
    val color: Color? = null,
    val icon: Drawable? = null,
    val clickAction: (() -> Unit)?,
)

data class TwoActionsCounterUi(
    val count: Int,
    val color: Color? = null,
    val upvoteAction: (() -> Unit)?,
    val downvoteAction: (() -> Unit)?,
)

data class ToggleButtonUi(
    val isToggled: Boolean,
    val clickAction: (() -> Unit)?,
    val isVisible: Boolean = true,
)
