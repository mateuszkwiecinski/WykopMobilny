package io.github.wykopmobilny.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import io.github.wykopmobilny.R

fun exitConfirmationDialog(context: Context, callback: () -> Unit): AlertDialog? = AlertDialog.Builder(context).run {
    setTitle(R.string.confirm_exit)
    setPositiveButton(android.R.string.ok) { _, _ -> callback.invoke() }
    setNeutralButton(android.R.string.cancel, null)

    create()
}
