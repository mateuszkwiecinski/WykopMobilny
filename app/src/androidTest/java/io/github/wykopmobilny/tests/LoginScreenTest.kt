package io.github.wykopmobilny.tests

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.wykopmobilny.R
import io.github.wykopmobilny.TestApp
import io.github.wykopmobilny.tests.pages.MainPage
import io.github.wykopmobilny.tests.responses.blacklist
import io.github.wykopmobilny.tests.responses.callsOnAppStart
import io.github.wykopmobilny.tests.responses.connectPage
import io.github.wykopmobilny.tests.responses.profile
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest : BaseActivityTest() {

    @Test
    fun happyPath() {
        mockWebServerRule.callsOnAppStart()
        launchActivity<MainNavigationActivity>()
        MainPage.openDrawer()

        mockWebServerRule.connectPage()
        mockWebServerRule.profile()
        TestApp.instance.cookieProvider.cookies += "http://localhost:8000" to ""
        mockWebServerRule.blacklist()
        mockWebServerRule.callsOnAppStart()
        MainPage.tapDrawerOption(R.id.login)
        Espresso.onIdle()

        MainPage.openDrawer()
        MainPage.tapDrawerOption(R.id.logout)
    }
}
