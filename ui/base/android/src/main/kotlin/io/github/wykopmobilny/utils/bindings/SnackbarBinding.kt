package io.github.wykopmobilny.utils.bindings

import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn

suspend fun Flow<String?>.collectSnackbar(target: View) {
    var snack: Snackbar? = null
    distinctUntilChangedBy { it }
        .flowOn(AppDispatchers.Default)
        .collect { message ->
            snack?.dismiss()
            snack = if (message == null) {
                null
            } else {
                Snackbar.make(target, message, Snackbar.LENGTH_INDEFINITE)
            }
            snack?.show()
        }
}
