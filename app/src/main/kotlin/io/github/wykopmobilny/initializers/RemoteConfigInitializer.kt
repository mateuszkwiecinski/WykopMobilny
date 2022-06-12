package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.wykopmobilny.BuildConfig
import io.github.wykopmobilny.initializers.RemoteConfigKeys.API_APP_KEY
import io.github.wykopmobilny.initializers.RemoteConfigKeys.API_APP_SECRET
import io.github.wykopmobilny.initializers.RemoteConfigKeys.BLACKLIST_FLEX_INTERVAL
import io.github.wykopmobilny.initializers.RemoteConfigKeys.BLACKLIST_REFRESH_INTERVAL
import io.github.wykopmobilny.initializers.RemoteConfigKeys.NOTIFICATIONS_ENABLED
import kotlin.time.Duration.Companion.days

internal class RemoteConfigInitializer : Initializer<FirebaseRemoteConfig> {

    override fun create(context: Context): FirebaseRemoteConfig {
        FirebaseRemoteConfig.getInstance().setDefaultsAsync(
            mapOf(
                API_APP_KEY to BuildConfig.APP_KEY,
                API_APP_SECRET to BuildConfig.APP_SECRET,
                BLACKLIST_REFRESH_INTERVAL to 7.days.inWholeMilliseconds,
                BLACKLIST_FLEX_INTERVAL to 1.days.inWholeMilliseconds,
                NOTIFICATIONS_ENABLED to false,
            ),
        )

        FirebaseRemoteConfig.getInstance().fetchAndActivate()

        return FirebaseRemoteConfig.getInstance()
    }

    override fun dependencies() = listOf(LoggingInitializer::class.java)
}

object RemoteConfigKeys {
    const val API_APP_KEY = "wykop_app_key"
    const val API_APP_SECRET = "wykop_app_secret"
    const val BLACKLIST_REFRESH_INTERVAL = "wykop_blacklist_refresh_interval"
    const val BLACKLIST_FLEX_INTERVAL = "wykop_blacklist_flex_interval"
    const val NOTIFICATIONS_ENABLED = "wykop_notifications_enabled"
    const val YOUTUBE_KEY = "wykop_youtube_key"
}
