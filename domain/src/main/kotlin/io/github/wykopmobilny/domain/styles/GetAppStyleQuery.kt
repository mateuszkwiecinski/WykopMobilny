package io.github.wykopmobilny.domain.styles

import io.github.wykopmobilny.domain.navigation.NightModeState
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.prefs.GetAppearanceSectionPreferences
import io.github.wykopmobilny.styles.AppliedStyleUi
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
                    style = findAppStyle(appearance.appTheme, appearance.isAmoledTheme),
                    edgeSlidingBehaviorEnabled = !appearance.disableEdgeSlide,
                )
            }
            .distinctUntilChanged()

    private suspend fun findAppStyle(appTheme: SavedAppTheme, isAmoledTheme: Boolean) =
        when (appTheme) {
            SavedAppTheme.Auto ->
                if (isSystemDark()) {
                    findDarkMode(isAmoledTheme)
                } else {
                    AppliedStyleUi.Light
                }
            SavedAppTheme.Light -> AppliedStyleUi.Light
            SavedAppTheme.Dark -> findDarkMode(isAmoledTheme)
        }

    private fun findDarkMode(isAmoledTheme: Boolean) =
        if (isAmoledTheme) {
            AppliedStyleUi.DarkAmoled
        } else {
            AppliedStyleUi.Dark
        }

    private suspend fun isSystemDark() =
        when (systemSettingsDetector.getNightModeState()) {
            NightModeState.Enabled -> true
            NightModeState.Disabled,
            NightModeState.Unknown,
            -> false
        }
}
