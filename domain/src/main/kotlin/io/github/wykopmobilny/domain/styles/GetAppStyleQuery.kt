package io.github.wykopmobilny.domain.styles

import io.github.wykopmobilny.domain.navigation.NightModeState
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.prefs.GetAppearanceSectionPreferences
import io.github.wykopmobilny.styles.ApplicableStyleUi
import io.github.wykopmobilny.styles.GetAppStyle
import io.github.wykopmobilny.styles.StyleUi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetAppStyleQuery @Inject constructor(
    private val getAppearanceSectionPreferences: GetAppearanceSectionPreferences,
    private val systemSettingsDetector: SystemSettingsDetector,
) : GetAppStyle {

    override fun invoke() = getAppearanceSectionPreferences()
        .map { appearance ->
            StyleUi(
                style = findAppStyle(appearance.appThemePreference, appearance.isAmoledTheme),
                edgeSlidingBehaviorEnabled = !appearance.disableEdgeSlide,
            )
        }
        .distinctUntilChanged()

    private suspend fun findAppStyle(appThemePreference: AppThemePreference, isAmoledTheme: Boolean) = when (appThemePreference) {
        AppThemePreference.Auto ->
            if (isSystemDark()) {
                findDarkMode(isAmoledTheme)
            } else {
                ApplicableStyleUi.Light
            }
        AppThemePreference.Light -> ApplicableStyleUi.Light
        AppThemePreference.Dark -> findDarkMode(isAmoledTheme)
    }

    private fun findDarkMode(isAmoledTheme: Boolean) = if (isAmoledTheme) {
        ApplicableStyleUi.DarkAmoled
    } else {
        ApplicableStyleUi.Dark
    }

    private suspend fun isSystemDark() = when (systemSettingsDetector.getNightModeState()) {
        NightModeState.Enabled -> true
        NightModeState.Disabled,
        NightModeState.Unknown,
        -> false
    }
}
