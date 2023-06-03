package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.navigation.NavigationMode
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.settings.FontSize
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.domain.styles.AppThemePreference
import io.github.wykopmobilny.kotlin.AppScopes
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

internal class GetAppearanceSectionPreferences @Inject constructor(
    private val appStorage: AppStorage,
    private val systemSettingsDetector: SystemSettingsDetector,
    private val appScopes: AppScopes,
) {

    operator fun invoke(): SharedFlow<AppearanceSection> = combine(
        appStorage.get(UserSettings.appTheme).map { it ?: AppThemePreference.Auto },
        appStorage.get(UserSettings.useAmoledTheme).map { it ?: false },
        appStorage.get(UserSettings.font).map { it ?: FontSize.Normal },
        appStorage.get(UserSettings.defaultScreen).map { it ?: MainScreen.Promoted },
        appStorage.get(UserSettings.disableEdgeSlide).map { it ?: findDefaultEdgeSlide() },
    ) { appTheme, useAmoledTheme, fontSize, defaultScreen, disableEdgeSlide ->
        AppearanceSection(
            appThemePreference = appTheme,
            isAmoledTheme = useAmoledTheme,
            defaultScreen = defaultScreen,
            defaultFont = fontSize,
            disableEdgeSlide = disableEdgeSlide,
        )
    }
        .shareIn(
            scope = appScopes.applicationScope,
            started = SharingStarted.Lazily,
            replay = 0,
        )

    private suspend fun findDefaultEdgeSlide() = when (systemSettingsDetector.getNavigationMode()) {
        NavigationMode.ThreeButtons,
        NavigationMode.TwoButtons,
        NavigationMode.Unknown,
        -> false
        NavigationMode.FullScreenGesture,
        -> true
    }
}

internal data class AppearanceSection(
    val appThemePreference: AppThemePreference,
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
