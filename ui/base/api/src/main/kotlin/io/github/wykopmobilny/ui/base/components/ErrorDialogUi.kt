package io.github.wykopmobilny.ui.base.components

data class ErrorDialogUi(
    val error: Throwable,
    val retryAction: (() -> Unit)?,
    val dismissAction: () -> Unit,
)

data class InfoDialogUi(
    val title: String,
    val message: CharSequence,
    val dismissAction: () -> Unit,
)
