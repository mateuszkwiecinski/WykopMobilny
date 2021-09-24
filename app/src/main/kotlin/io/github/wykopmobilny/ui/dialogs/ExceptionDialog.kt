package io.github.wykopmobilny.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.errorhandler.WykopExceptionParser
import io.github.wykopmobilny.base.BaseActivity
import okio.IOException
import javax.net.ssl.SSLException

fun Context.showExceptionDialog(ex: Throwable) {
    if (this is BaseActivity && isRunning) {
        exceptionDialog(this, ex)?.show()
    }
    when (ex) {
        is SSLException -> Napier.e("Ssl exception", ex)
        is IOException -> Napier.w("Io exception", ex)
        else -> Napier.e("Unknown", ex)
    }
}

private fun exceptionDialog(context: Context, e: Throwable): AlertDialog? {
    val message = when {
        e is WykopExceptionParser.WykopApiException -> "${e.message} (${e.code})"
        e.message.isNullOrEmpty() -> e.toString()
        else -> e.message
    }
    AlertDialog.Builder(context).run {
        setTitle(context.getString(R.string.error_occured))
        setMessage(message)
        setPositiveButton(android.R.string.ok, null)
        return create()
    }
}
