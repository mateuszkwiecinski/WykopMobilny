package io.github.wykopmobilny.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import io.github.wykopmobilny.TestApp
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.UserSession
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

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(IdlingResourcesRule())
        .around(CleanupRule())
        .around(DispatcherIdlerRule())
        .around(mockWebServerRule)

    protected fun logUserIn() = runBlocking {
        val storages = TestApp.instance.storages
        storages.sessionStorage().updateSession(UserSession(login = "fixture-user", token = "fixture_token"))
        storages.userInfoStorage().updateLoggedUser(
            LoggedUserInfo(
                id = "Fixture name",
                userToken = "fixture_token",
                avatarUrl = "",
                backgroundUrl = null,
            ),
        )
        Espresso.onIdle()
    }

    protected fun launchLoggedInApp(): ActivityScenario<MainNavigationActivity> {
        logUserIn()
        mockWebServerRule.callsOnAppStart()
        return launchActivity()
    }
}
