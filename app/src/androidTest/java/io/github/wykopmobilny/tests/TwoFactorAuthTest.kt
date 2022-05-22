package io.github.wykopmobilny.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.wykopmobilny.R
import io.github.wykopmobilny.tests.pages.MainPage
import io.github.wykopmobilny.tests.responses.callsOnAppStart
import io.github.wykopmobilny.tests.responses.twoFactorAuthErrorWrongCode
import io.github.wykopmobilny.tests.responses.twoFactorAuthSuccess
import io.github.wykopmobilny.tests.responses.upcomingErrorTwoFactorNeeded
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwoFactorAuthTest : BaseActivityTest() {

    @Test
    fun goesThroughTwoFactorAuthFlow() {
        launchLoggedInApp()
        MainPage.openDrawer()

        mockWebServerRule.upcomingErrorTwoFactorNeeded()
        MainPage.tapDrawerOption(R.id.nav_wykopalisko)
        ErrorDialogRegion.assertVisible(
            text = "[1101] Wymagana dwustopniowa autoryzacja, sprawdź czy nie jest dostępna aktualizacja aplikacji",
            interop = true,
        )

        ErrorDialogRegion.tapButton("Uwierzytelnij", interop = true)
        TwoFactorAuthPage.assertVisible()

        mockWebServerRule.twoFactorAuthErrorWrongCode()
        TwoFactorAuthPage.typeCode("123456")
        TwoFactorAuthPage.tapCtaButton()
        ErrorDialogRegion.assertVisible("[1102] Niepoprawny kod autoryzacji")
        ErrorDialogRegion.tapButton()

        mockWebServerRule.twoFactorAuthSuccess()
        mockWebServerRule.callsOnAppStart()
        TwoFactorAuthPage.tapCtaButton()
        MainPage.assertVisible()
    }
}
