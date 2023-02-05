package io.github.wykopmobilny.utils

import android.text.Selection
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.asExecutor

fun <T> asyncDifferConfig(diff: DiffUtil.ItemCallback<T>): AsyncDifferConfig<T> = AsyncDifferConfig.Builder(diff)
    .setBackgroundThreadExecutor(AppDispatchers.Default.asExecutor())
    .build()

/**
 * 😡
 * https://stackoverflow.com/questions/37566303/edittext-giving-error-textview-does-not-support-text-selection-selection-canc
 */
fun TextView.fixTextIsSelectableWhenUnderRecyclerView() = runCatching {
    Selection.removeSelection(text.toSpannable())
    val temp = movementMethod
    movementMethod = null
    movementMethod = temp
}
    .onFailure { Napier.w("I hate android", it) }
