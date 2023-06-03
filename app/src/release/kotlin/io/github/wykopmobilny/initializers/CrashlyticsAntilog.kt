package io.github.wykopmobilny.initializers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

internal class CrashlyticsAntilog : Antilog() {

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        message?.let { FirebaseCrashlytics.getInstance().log(message) }

        if (priority >= LogLevel.WARNING) {
            throwable?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        }
    }
}
