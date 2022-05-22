package io.github.wykopmobilny.utils.bindings

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.android.R
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.InfoDialogUi
import io.github.wykopmobilny.utils.theme.AppTheme
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

private fun stubErrorDialogUi() = ErrorDialogUi(
    error = IllegalStateException("Henlo"),
    retryAction = { },
    dismissAction = { },
)

private fun stubInfoDialogUi() = InfoDialogUi(
    title = "text",
    message = HtmlCompat.fromHtml("sorry <b>or</b> not sorry", HtmlCompat.FROM_HTML_MODE_COMPACT),
    dismissAction = { },
)

@Preview
@Composable
private fun TestErrorDialog() {
    AppTheme(appTheme = AppTheme.Light) {
        AppErrorDialog(stubErrorDialogUi())
    }
}

@Preview
@Composable
private fun TestInfoDialog() {
    AppTheme(appTheme = AppTheme.Dark) {
        AppInfoDialog(stubInfoDialogUi())
    }
}

@Composable
fun AppErrorDialog(dialogUi: ErrorDialogUi) {
    AlertDialog(
        onDismissRequest = { dialogUi.dismissAction() },
        title = { Text(text = stringResource(id = R.string.error_dialog_title)) },
        text = { Text(text = dialogUi.error.message ?: dialogUi.error.toString()) },
        confirmButton = {
            TextButton(
                onClick = { dialogUi.dismissAction() },
                content = { Text(stringResource(id = R.string.error_dialog_confirm)) },
            )
        },
        dismissButton = if (dialogUi.retryAction != null) {
            {
                TextButton(
                    onClick = { dialogUi.retryAction?.invoke() },
                    content = { Text(stringResource(id = R.string.error_dialog_retry)) },
                )
            }
        } else {
            null
        },
    )
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

@Composable
fun AppInfoDialog(dialogUi: InfoDialogUi) {
    AlertDialog(
        onDismissRequest = { dialogUi.dismissAction() },
        title = { Text(text = dialogUi.title) },
        text = { Text(text = AnnotatedString(text = dialogUi.message.toString())) },
        confirmButton = {
            TextButton(
                onClick = { dialogUi.dismissAction() },
                content = { Text(stringResource(id = R.string.error_dialog_confirm)) },
            )
        },
        dismissButton = null,
    )
}
