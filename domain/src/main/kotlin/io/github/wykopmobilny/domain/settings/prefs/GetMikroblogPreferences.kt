package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetMikroblogPreferences @Inject constructor(
    private val appStorage: AppStorage,
) {

    operator fun invoke() = combine(
        appStorage.get(UserSettings.mikroblogScreen),
        appStorage.get(UserSettings.cutLongEntries),
        appStorage.get(UserSettings.openSpoilersInDialog),
    ) { defaultScreen, cutLongEntries, openSpoilersInDialog ->
        MikroblogPreferences(
            defaultScreen = defaultScreen ?: MikroblogScreen.Newest,
            cutLongEntries = cutLongEntries ?: true,
            openSpoilersInDialog = openSpoilersInDialog ?: true,
        )
    }
}

internal data class MikroblogPreferences(
    val defaultScreen: MikroblogScreen,
    val cutLongEntries: Boolean,
    val openSpoilersInDialog: Boolean,
)

internal enum class MikroblogScreen {
    Active,
    Newest,
    SixHours,
    TwelveHours,
    TwentyFourHours,
}
