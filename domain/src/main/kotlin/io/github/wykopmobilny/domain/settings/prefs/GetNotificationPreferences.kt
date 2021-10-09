package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.domain.startup.AppConfig
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.time.Duration

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
        FifteenMinutes(Duration.minutes(15)),
        ThirtyMinutes(Duration.minutes(30)),
        OneHour(Duration.hours(1)),
        TwoHours(Duration.hours(2)),
        FourHours(Duration.hours(4)),
        EightHours(Duration.hours(8)),
    }
}
