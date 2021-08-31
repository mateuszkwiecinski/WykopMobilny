package io.github.wykopmobilny.ui.settings.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.unboundedHeight
import io.github.wykopmobilny.ui.settings.FilteringUi
import io.github.wykopmobilny.ui.settings.GeneralPreferencesUi
import io.github.wykopmobilny.ui.settings.GeneralPreferencesUi.NotificationsUi.RefreshPeriodUi
import org.junit.Test

internal class AppearancePreferencesFragmentTest : BaseScreenshotTest() {

    override fun createFragment() = GeneralPreferencesFragment()

    @Test
    fun withoutBlacklist() {
        registerSettings(
            general = {
                GeneralPreferencesUi(
                    notifications = GeneralPreferencesUi.NotificationsUi(
                        notificationsEnabled = stubSetting(value = true),
                        notificationRefreshPeriod = stubListSetting(value = RefreshPeriodUi.FifteenMinutes),
                        exitConfirmation = stubSetting(value = true),
                    ),
                    filtering = FilteringUi(
                        showPlus18Content = stubSetting(value = true),
                        hideNsfwContent = stubSetting(value = true),
                        hideNewUserContent = stubSetting(value = true),
                        hideContentWithNoTags = stubSetting(value = true),
                        hideBlacklistedContent = stubSetting(value = true),
                        manageBlackList = null,
                        useEmbeddedBrowser = stubSetting(value = true),
                        clearSearchHistory = { },
                    ),
                )
            },
        )
        record(size = unboundedHeight())
    }

    @Test
    fun withBlacklist() {
        registerSettings(
            general = {
                GeneralPreferencesUi(
                    notifications = GeneralPreferencesUi.NotificationsUi(
                        notificationsEnabled = stubSetting(value = false),
                        notificationRefreshPeriod = stubListSetting(value = RefreshPeriodUi.TwoHours),
                        exitConfirmation = stubSetting(value = false),
                    ),
                    filtering = FilteringUi(
                        showPlus18Content = stubSetting(value = false),
                        hideNsfwContent = stubSetting(value = false),
                        hideNewUserContent = stubSetting(value = false),
                        hideContentWithNoTags = stubSetting(value = false),
                        hideBlacklistedContent = stubSetting(value = false),
                        manageBlackList = { },
                        useEmbeddedBrowser = stubSetting(value = false),
                        clearSearchHistory = { },
                    ),
                )
            },
        )
        record(size = unboundedHeight())
    }
}
