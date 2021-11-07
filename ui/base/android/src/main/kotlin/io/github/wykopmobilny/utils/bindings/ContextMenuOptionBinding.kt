package io.github.wykopmobilny.utils.bindings

import android.view.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy

suspend fun <T : Enum<T>> Flow<List<ContextMenuOptionUi<T>>>.collectMenuOptions(
    toolbar: MaterialToolbar,
    mapping: (T) -> Pair<Int, Int?>,
) {
    distinctUntilChangedBy { it.map(ContextMenuOptionUi<T>::option) }.collect { options ->
        toolbar.menu.clear()
        options.forEach { menuOption ->
            val (textRes, imageRes) = mapping(menuOption.option)
            toolbar.menu.add(textRes).apply {
                setOnMenuItemClickListener { menuOption.onClick(); true }
                imageRes?.let(::setIcon)?.let { setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) }
            }
        }
    }
}
