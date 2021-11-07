package io.github.wykopmobilny.ui.base.components

data class SwipeRefreshUi(
    val isRefreshing: Boolean,
    val refreshAction: () -> Unit,
)
