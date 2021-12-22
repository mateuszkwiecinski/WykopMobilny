package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.domain.startup.AppConfig
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal class GetNotificationPreferences @Inject constructor(
    private val appStorage: AppStorage,
    private val appConfig: AppConfig,
) {

    operator fun invoke() = combine(
        appStorage.get(UserSettings.notificationsEnabled),
        appStorage.get(UserSettings.notificationsRefreshPeriod),
        appStorage.get(UserSettings.exitConfirmation),
    ) { notificationsEnabled, notificationRefreshPeriod, exitConfirmation ->
        NotificationsPreferences(
            notificationsEnabled = notificationsEnabled ?: appConfig.notificationsEnabled,
            notificationRefreshPeriod = notificationRefreshPeriod?.let(::findRefreshPeriod)
                ?: NotificationsPreferences.RefreshPeriod.FifteenMinutes,
            exitConfirmation = exitConfirmation ?: true,
        )
    }
        .distinctUntilChanged()

    private fun findRefreshPeriod(duration: Duration) =
        NotificationsPreferences.RefreshPeriod.values()
            .sortedByDescending { it.duration }
            .firstOrNull { it.duration <= duration }
}

internal data class NotificationsPreferences(
    val notificationsEnabled: Boolean,
    val notificationRefreshPeriod: RefreshPeriod,
    val exitConfirmation: Boolean,
) {

    @Suppress("MagicNumber")
    enum class RefreshPeriod(val duration: Duration) {
        FifteenMinutes(15.minutes),
        ThirtyMinutes(30.minutes),
        OneHour(1.hours),
        TwoHours(2.hours),
        FourHours(4.hours),
        EightHours(8.hours),
    }
}
