package io.github.wykopmobilny.domain.navigation.android

import android.app.Application
import android.content.res.Configuration
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.domain.navigation.NavigationMode
import io.github.wykopmobilny.domain.navigation.NightModeState
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidSystemSettingsDetector @Inject constructor(
    private val application: Application,
) : SystemSettingsDetector {

    override suspend fun getNightModeState() = withContext(Dispatchers.Default) {
        when (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> NightModeState.Enabled
            Configuration.UI_MODE_NIGHT_NO -> NightModeState.Disabled
            Configuration.UI_MODE_NIGHT_UNDEFINED -> NightModeState.Unknown
            else -> NightModeState.Unknown
        }
    }

    override suspend fun getNavigationMode(): NavigationMode = withContext(Dispatchers.Default) {
        runCatching {
            val resourceId = application.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
                .takeIf { it > 0 }
                ?: return@runCatching NavigationMode.Unknown.also { Napier.i("There is no config_navBarInteractionMode") }

            when (application.resources.getInteger(resourceId)) {
                0 -> NavigationMode.ThreeButtons
                1 -> NavigationMode.TwoButtons
                2 -> NavigationMode.FullScreenGesture
                else -> NavigationMode.Unknown.also { Napier.i("Unknown resourceId=$resourceId") }
            }
        }
            .onFailure { Napier.w("getNavigationMode failed", throwable = it) }
            .getOrDefault(NavigationMode.Unknown)
    }
}
