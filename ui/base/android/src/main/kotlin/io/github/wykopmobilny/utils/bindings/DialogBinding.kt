package io.github.wykopmobilny.utils.bindings

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.wykopmobilny.ui.base.android.R
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy

suspend fun Flow<ErrorDialogUi?>.collectErrorDialog(context: Context) {
    var dialog: AlertDialog? = null
    distinctUntilChangedBy { it?.error }
        .collect { dialogUi ->
            dialog?.dismiss()
            dialog = if (dialogUi != null) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.error_dialog_title)
                    .setMessage(dialogUi.error.message ?: dialogUi.error.toString())
                    .setNegativeButton(R.string.error_dialog_retry) { _, _ -> dialogUi.retryAction() }
                    .setPositiveButton(R.string.error_dialog_confirm) { _, _ -> dialogUi.dismissAction() }
                    .setOnCancelListener { dialogUi.dismissAction() }
                    .show()
            } else {
                null
            }
        }
}
