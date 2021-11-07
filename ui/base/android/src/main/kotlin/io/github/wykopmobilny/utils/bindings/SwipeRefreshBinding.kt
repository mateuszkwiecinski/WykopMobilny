package io.github.wykopmobilny.utils.bindings

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

suspend fun Flow<SwipeRefreshUi>.collectSwipeRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
    collect { ui ->
        swipeRefreshLayout.isRefreshing = ui.isRefreshing
        swipeRefreshLayout.setOnRefreshListener { ui.refreshAction() }
    }
}
