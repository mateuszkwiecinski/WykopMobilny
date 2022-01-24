package io.github.wykopmobilny.tests

import io.github.wykopmobilny.tests.responses.promotedErrorTwoFactorNeeded
import io.github.wykopmobilny.tests.responses.twoFactorAuthErrorWrongCode
import io.github.wykopmobilny.tests.responses.twoFactorAuthSuccess
import org.junit.Before

class TwoFactorAuthTest : BaseActivityTest() {

    @Before
    fun setUp() {
        launchLoggedInApp()
        mockWebServerRule.promotedErrorTwoFactorNeeded()

        mockWebServerRule.twoFactorAuthErrorWrongCode()

        mockWebServerRule.twoFactorAuthSuccess()
    }
}
