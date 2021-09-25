package io.github.wykopmobilny.ui.base.components

data class ErrorDialogUi(
    val error: Throwable,
    val retryAction: (() -> Unit)?,
    val dismissAction: () -> Unit,
)
