package io.github.wykopmobilny.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.wykopmobilny.R
import io.github.wykopmobilny.tests.pages.BlacklistPage
import io.github.wykopmobilny.tests.pages.MainPage
import io.github.wykopmobilny.tests.pages.SettingsPage
import io.github.wykopmobilny.tests.responses.blacklist
import io.github.wykopmobilny.tests.responses.unblockTag
import io.github.wykopmobilny.tests.responses.unblockUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlacklistTest : BaseActivityTest() {

    @Before
    fun setUp() {
        launchLoggedInApp()
        MainPage.openDrawer()
        MainPage.tapDrawerOption(R.id.nav_settings)
        mockWebServerRule.blacklist()
        SettingsPage.tapBlacklistSettings()
    }

    @Test
    fun unblockTag() {
        BlacklistPage.assertVisible()
        BlacklistPage.assertBlockedTagVisible("365styczen")

        mockWebServerRule.unblockTag("365styczen")
        BlacklistPage.tapUnblockTag("365styczen")

        BlacklistPage.assertBlockedTagNotVisible("365styczen")
    }

    @Test
    fun unblockUser() {
        BlacklistPage.tapUsersTab()
        BlacklistPage.assertBlockedUserVisible("fixture_user_1")

        mockWebServerRule.unblockUser(user = "fixture_user_1")
        BlacklistPage.tapUnblockUser("fixture_user_1")
        BlacklistPage.assertBlockedUserNotVisible("fixture_user_1")

        BlacklistPage.tapTagsTab()
        BlacklistPage.assertBlockedTagVisible("zwierzaczki")
    }
}
