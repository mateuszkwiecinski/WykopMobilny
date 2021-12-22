package io.github.wykopmobilny.domain.styles

import io.github.wykopmobilny.domain.navigation.NightModeState
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.prefs.GetAppearanceSectionPreferences
import io.github.wykopmobilny.styles.AppThemeUi
import io.github.wykopmobilny.styles.GetAppStyle
import io.github.wykopmobilny.styles.StyleUi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetAppStyleQuery @Inject constructor(
    private val getAppearanceSectionPreferences: GetAppearanceSectionPreferences,
    private val systemSettingsDetector: SystemSettingsDetector,
) : GetAppStyle {

    override fun invoke() =
        getAppearanceSectionPreferences()
            .map { appearance ->
                StyleUi(
                    theme = mapTheme(appearance.appTheme, appearance.isAmoledTheme),
                    edgeSlidingBehaviorEnabled = !appearance.disableEdgeSlide,
                )
            }.distinctUntilChanged()

    private suspend fun mapTheme(appTheme: AppTheme, amoledTheme: Boolean) =
        when (appTheme) {
            AppTheme.Auto ->
                if (isSystemDark()) maybeAmoledDark(amoledTheme)
                else AppThemeUi.Light
            AppTheme.Light -> AppThemeUi.Light
            AppTheme.Dark -> maybeAmoledDark(amoledTheme)
        }

    private fun maybeAmoledDark(useAmoledTheme: Boolean) =
        if (useAmoledTheme) AppThemeUi.DarkAmoled
        else AppThemeUi.Dark

    private suspend fun isSystemDark() =
        when (systemSettingsDetector.getNightModeState()) {
            NightModeState.Enabled -> true
            NightModeState.Disabled,
            NightModeState.Unknown,
            -> false
        }
}
