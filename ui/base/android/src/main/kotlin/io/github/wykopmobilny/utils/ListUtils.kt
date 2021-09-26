package io.github.wykopmobilny.utils

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.asExecutor

fun <T> asyncDifferConfig(diff: DiffUtil.ItemCallback<T>) = AsyncDifferConfig.Builder(diff)
    .setBackgroundThreadExecutor(AppDispatchers.Default.asExecutor())
    .build()
