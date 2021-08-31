package io.github.wykopmobilny.domain.styles

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.navigation.NightModeState
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class GetAppTheme @Inject constructor(
    private val appStorage: AppStorage,
    private val systemSettingsDetector: SystemSettingsDetector,
) {

    operator fun invoke() =
        combine(
            appStorage.get(UserSettings.darkTheme),
            appStorage.get(UserSettings.useAmoledTheme),
        ) { darkTheme, amoledTheme ->
            if (darkTheme ?: shouldBeDarkByDefault()) {
                if (amoledTheme == true) {
                    AppTheme.DarkAmoled
                } else {
                    AppTheme.Dark
                }
            } else {
                AppTheme.Light
            }
        }
            .distinctUntilChanged()

    private suspend fun shouldBeDarkByDefault() =
        when (systemSettingsDetector.getNightModeState()) {
            NightModeState.Enabled -> true
            NightModeState.Disabled,
            NightModeState.Unknown,
            -> false
        }
}
