package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.wykopmobilny.BuildConfig
import kotlin.time.Duration

internal class RemoteConfigInitializer : Initializer<FirebaseRemoteConfig> {

    override fun create(context: Context): FirebaseRemoteConfig {
        FirebaseRemoteConfig.getInstance().setDefaultsAsync(
            mapOf(
                "wykop_app_key" to BuildConfig.APP_KEY,
                "wykop_app_secret" to BuildConfig.APP_SECRET,
                "wykop_blacklist_refresh_interval" to Duration.days(7).inWholeMilliseconds,
                "wykop_blacklist_flex_interval" to Duration.days(1).inWholeMilliseconds,
            ),
        )

        FirebaseRemoteConfig.getInstance().fetchAndActivate()

        return FirebaseRemoteConfig.getInstance()
    }

    override fun dependencies() = listOf(LoggingInitializer::class.java)
}
