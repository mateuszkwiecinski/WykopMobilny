package io.github.wykopmobilny.utils.bindings

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.ui.base.android.R
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.InfoDialogUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn

suspend fun Flow<ErrorDialogUi?>.collectErrorDialog(context: Context) {
    var dialog: AlertDialog? = null
    distinctUntilChangedBy { it?.error }
        .flowOn(AppDispatchers.Default)
        .collect { dialogUi ->
            dialog?.dismiss()
            dialog = if (dialogUi != null) {
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(R.string.error_dialog_title)
                    setMessage(dialogUi.error.message ?: dialogUi.error.toString())
                    dialogUi.retryAction?.let { retry -> setNegativeButton(R.string.error_dialog_retry) { _, _ -> retry() } }
                    setPositiveButton(R.string.error_dialog_confirm) { _, _ -> dialogUi.dismissAction() }
                    setOnCancelListener { dialogUi.dismissAction() }
                }
                    .show()
            } else {
                null
            }
        }
}

suspend fun Flow<InfoDialogUi?>.collectInfoDialog(context: Context) {
    var dialog: AlertDialog? = null
    distinctUntilChangedBy { it?.title + it?.message }
        .flowOn(AppDispatchers.Default)
        .collect { dialogUi ->
            dialog?.dismiss()
            dialog = if (dialogUi != null) {
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(dialogUi.title)
                    setMessage(dialogUi.message)
                    setPositiveButton(R.string.error_dialog_confirm) { _, _ -> dialogUi.dismissAction() }
                    setOnCancelListener { dialogUi.dismissAction() }
                }
                    .show().also {
                        it?.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
                    }
            } else {
                null
            }
        }
}
