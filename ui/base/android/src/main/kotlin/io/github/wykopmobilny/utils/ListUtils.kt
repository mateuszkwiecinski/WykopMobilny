package io.github.wykopmobilny.utils

import android.widget.TextView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.asExecutor

fun <T> asyncDifferConfig(diff: DiffUtil.ItemCallback<T>) = AsyncDifferConfig.Builder(diff)
    .setBackgroundThreadExecutor(AppDispatchers.Default.asExecutor())
    .build()


/**
 * 😡
 * https://stackoverflow.com/questions/37566303/edittext-giving-error-textview-does-not-support-text-selection-selection-canc
 */
fun TextView.fixTextIsSelectableWhenUnderRecyclerView() {
    setTextIsSelectable(false)
    measure(-1, -1)
    setTextIsSelectable(true)
}
