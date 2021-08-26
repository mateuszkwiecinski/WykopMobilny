package io.github.wykopmobilny.domain.navigation

interface SystemSettingsDetector {

    suspend fun getNightModeState(): NightModeState

    suspend fun getNavigationMode(): NavigationMode
}

enum class NightModeState {
    Enabled,
    Disabled,
    Unknown,
}

enum class NavigationMode {
    ThreeButtons,
    TwoButtons,
    FullScreenGesture,
    Unknown,
}
