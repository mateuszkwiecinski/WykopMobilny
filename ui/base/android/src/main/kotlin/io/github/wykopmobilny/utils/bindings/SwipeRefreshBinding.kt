package io.github.wykopmobilny.utils.bindings

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn

suspend fun Flow<SwipeRefreshUi>.collectSwipeRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
    distinctUntilChangedBy { it.isRefreshing }
        .flowOn(AppDispatchers.Default)
        .collect { ui ->
            swipeRefreshLayout.isRefreshing = ui.isRefreshing
            swipeRefreshLayout.setOnRefreshListener { ui.refreshAction() }
        }
}
