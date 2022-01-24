package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal class CrashlyticsInitializer : Initializer<FirebaseCrashlytics> {

    override fun create(context: Context): FirebaseCrashlytics {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        return crashlytics
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}


