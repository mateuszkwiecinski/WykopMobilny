package io.github.wykopmobilny.domain.startup

import io.github.wykopmobilny.domain.settings.prefs.GetNotificationPreferences
import io.github.wykopmobilny.work.WorkScheduler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class InitializeApp @Inject internal constructor(
    private val workScheduler: WorkScheduler,
    private val appConfig: AppConfig,
    private val getNotificationPreferences: GetNotificationPreferences,
) {

    suspend operator fun invoke() {
        coroutineScope {
            launch {
                workScheduler.setupBlacklistRefresh(
                    repeatInterval = appConfig.blacklistRefreshInterval,
                    flexDuration = appConfig.blacklistFlexInterval,
                )
            }
            launch {
                getNotificationPreferences()
                    .map { it.notificationsEnabled to it.notificationRefreshPeriod }
                    .distinctUntilChanged()
                    .collect { (enabled, refreshPeriod) ->
                        workScheduler.cancelNotificationsCheck()
                        if (enabled) {
                            workScheduler.setupNotificationsCheck(
                                repeatInterval = refreshPeriod.duration,
                            )
                        }
                    }
            }
        }
    }
}
