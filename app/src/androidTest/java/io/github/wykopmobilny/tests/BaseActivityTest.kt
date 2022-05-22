package io.github.wykopmobilny.tests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import io.github.wykopmobilny.TestApp
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.UserSession
import io.github.wykopmobilny.tests.pages.ErrorDialogRegion
import io.github.wykopmobilny.tests.pages.TwoFactorAuthPage
import io.github.wykopmobilny.tests.responses.callsOnAppStart
import io.github.wykopmobilny.tests.rules.CleanupRule
import io.github.wykopmobilny.tests.rules.DispatcherIdlerRule
import io.github.wykopmobilny.tests.rules.IdlingResourcesRule
import io.github.wykopmobilny.tests.rules.MockWebServerRule
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.RuleChain

abstract class BaseActivityTest {

    val mockWebServerRule = MockWebServerRule()

    private val composeTestRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(IdlingResourcesRule())
        .around(CleanupRule())
        .around(DispatcherIdlerRule())
        .around(mockWebServerRule)
        .around(composeTestRule)

    protected fun logUserIn() = runBlocking {
        val storages = TestApp.instance.storages
        storages.sessionStorage().updateSession(UserSession(login = "fixture-user", token = "fixture_token"))
        storages.userInfoStorage().updateLoggedUser(
            LoggedUserInfo(
                id = "Fixture name",
                userToken = "fixture_token",
                avatarUrl = "https://wykop.pl/cdn/avatarfixture-avatar.png",
                backgroundUrl = null,
            ),
        )
        Espresso.onIdle()
    }

    protected fun launchLoggedInApp() {
        logUserIn()
        mockWebServerRule.callsOnAppStart()
        launchActivity<MainNavigationActivity>()
        Espresso.onIdle()
    }

    protected val TwoFactorAuthPage = TwoFactorAuthPage(composeTestRule)
    protected val ErrorDialogRegion = ErrorDialogRegion(composeTestRule)
}
