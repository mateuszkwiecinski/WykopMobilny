package io.github.wykopmobilny

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

internal class CrashlyticsAntilog : Antilog() {

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        if (priority < LogLevel.WARNING) {
            return
        }

        message?.let { FirebaseCrashlytics.getInstance().log(message) }
        throwable?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }
}
