package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.BuildConfig
import io.github.wykopmobilny.CrashlyticsAntilog

internal class CrashlyticsInitializer : Initializer<FirebaseCrashlytics> {

    override fun create(context: Context): FirebaseCrashlytics {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        return crashlytics
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}

internal class LoggingInitializer : Initializer<Napier> {

    override fun create(context: Context): Napier {
        val logger = if (BuildConfig.DEBUG) {
            DebugAntilog()
        } else {
            CrashlyticsAntilog()
        }
        Napier.base(logger)

        return Napier
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
