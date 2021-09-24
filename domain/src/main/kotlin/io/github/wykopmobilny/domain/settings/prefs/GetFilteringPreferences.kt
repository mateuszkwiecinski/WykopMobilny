package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class GetFilteringPreferences @Inject constructor(
    private val appStorage: AppStorage,
) {

    operator fun invoke() = combine(
        appStorage.get(UserSettings.hidePlus18Content),
        appStorage.get(UserSettings.hideNsfwContent),
        appStorage.get(UserSettings.hideNewUserContent),
        appStorage.get(UserSettings.hideContentWithNoTags),
        appStorage.get(UserSettings.hideBlacklistedContent),
        appStorage.get(UserSettings.useEmbeddedBrowser),
    ) { items ->
        FilteringPreferences(
            hidePlus18Content = items[0] ?: true,
            hideNsfwContent = items[1] ?: true,
            hideNewUserContent = items[2] ?: false,
            hideContentWithNoTags = items[3] ?: false,
            hideBlacklistedContent = items[4] ?: false,
            useEmbeddedBrowser = items[5] ?: true,
        )
    }
        .distinctUntilChanged()
}

internal data class FilteringPreferences(
    val hidePlus18Content: Boolean,
    val hideNsfwContent: Boolean,
    val hideNewUserContent: Boolean,
    val hideContentWithNoTags: Boolean,
    val hideBlacklistedContent: Boolean,
    val useEmbeddedBrowser: Boolean,
)
