package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.navigation.NavigationMode
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.FontSize
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.domain.styles.AppTheme
import io.github.wykopmobilny.domain.styles.GetAppTheme
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetAppearanceSectionPreferences @Inject constructor(
    private val appStorage: AppStorage,
    private val getAppTheme: GetAppTheme,
    private val systemSettingsDetector: SystemSettingsDetector,
) {

    operator fun invoke() = combine(
        getAppTheme(),
        appStorage.get(UserSettings.useAmoledTheme),
        appStorage.get(UserSettings.font),
        appStorage.get(UserSettings.defaultScreen),
        appStorage.get(UserSettings.disableEdgeSlide),
    ) { currentAppTheme, amoledTheme, fontSize, defaultScreen, disableEdgeSlide ->
        val isDarkTheme = when (currentAppTheme) {
            AppTheme.Light -> false
            AppTheme.Dark -> true
            AppTheme.DarkAmoled -> true
        }
        val isAmoledTheme = amoledTheme == true
        val screen = defaultScreen ?: MainScreen.Promoted
        val font = fontSize ?: FontSize.Normal
        val disableEdgeSlideBehavior = disableEdgeSlide ?: findDefaultEdgeSlide()

        AppearanceSection(
            isDarkTheme = isDarkTheme,
            isAmoledTheme = isAmoledTheme,
            defaultScreen = screen,
            defaultFont = font,
            disableEdgeSlide = disableEdgeSlideBehavior,
        )
    }

    private suspend fun findDefaultEdgeSlide() =
        when (systemSettingsDetector.getNavigationMode()) {
            NavigationMode.ThreeButtons,
            NavigationMode.TwoButtons,
            NavigationMode.Unknown,
            -> false
            NavigationMode.FullScreenGesture,
            -> true
        }
}

internal data class AppearanceSection(
    val isDarkTheme: Boolean,
    val isAmoledTheme: Boolean,
    val defaultScreen: MainScreen,
    val defaultFont: FontSize,
    val disableEdgeSlide: Boolean,
)

internal enum class MainScreen {
    Promoted,
    Mikroblog,
    MyWykop,
    Hits,
}
