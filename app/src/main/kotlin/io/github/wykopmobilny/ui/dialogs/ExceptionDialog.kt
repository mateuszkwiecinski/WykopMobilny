package io.github.wykopmobilny.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.errorhandler.WykopExceptionParser
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.domain.errorhandling.KnownError
import io.github.wykopmobilny.ui.modules.twofactor.TwoFactorAuthorizationActivity
import okio.IOException
import javax.net.ssl.SSLException

fun Context.showExceptionDialog(throwable: Throwable) {
    if (this is BaseActivity && isRunning) {
        if (throwable is KnownError.TwoFactorAuthorizationRequired) {
            exceptionDialog(
                throwable = throwable,
                onPositive = R.string.open_2fa to { startActivity(TwoFactorAuthorizationActivity.createIntent(this)) },
            )
        } else {
            exceptionDialog(throwable = throwable)
        }
            .show()
    }
    when (throwable) {
        is KnownError -> Napier.i("Known error", throwable)
        is SSLException -> Napier.e("SSL error", throwable)
        is IOException -> Napier.w("IO error", throwable)
        else -> Napier.e("Unknown error", throwable)
    }
}

private fun Context.exceptionDialog(
    throwable: Throwable,
    onPositive: Pair<Int, () -> Unit> = android.R.string.ok to { },
): AlertDialog {
    val message = when {
        throwable is WykopExceptionParser.WykopApiException -> "${throwable.message} (${throwable.code})"
        throwable.message.isNullOrEmpty() -> throwable.toString()
        else -> throwable.message
    }
    val builder = AlertDialog.Builder(this).apply {
        setTitle(context.getString(R.string.error_occured))
        setMessage(message)
        setPositiveButton(onPositive.first) { _, _ -> onPositive.second() }
    }

    return builder.create()
}
