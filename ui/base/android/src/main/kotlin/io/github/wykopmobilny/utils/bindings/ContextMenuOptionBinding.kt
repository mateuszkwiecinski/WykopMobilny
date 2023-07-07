package io.github.wykopmobilny.utils.bindings

import android.view.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn

suspend fun Flow<List<ContextMenuOptionUi>>.collectMenuOptions(toolbar: MaterialToolbar) {
    distinctUntilChangedBy { it.map(ContextMenuOptionUi::label) + it.map(ContextMenuOptionUi::icon) }
        .flowOn(AppDispatchers.Default)
        .collect { options ->
            toolbar.menu.clear()
            options.forEach { menuOption ->
                toolbar.menu.add(menuOption.label).apply {
                    setOnMenuItemClickListener {
                        menuOption.onClick()
                        true
                    }
                    menuOption.icon?.drawableRes?.let(::setIcon)?.let { setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) }
                }
            }
        }
}
