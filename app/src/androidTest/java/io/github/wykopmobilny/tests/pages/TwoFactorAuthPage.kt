package io.github.wykopmobilny.tests.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible
import org.hamcrest.Matchers.startsWith

object TwoFactorAuthPage : Page {

    private val title = withText(startsWith("Wygląda na to że Twoje konto korzysta z uwierzytenienia dwuskładnikowego (2FA)"))
    private val codeInput = withHint("6 cyfrowy kod")
    private val ctaButton = withText("Weryfikuj")

    fun assertVisible() {
        onView(title).waitVisible()
    }

    fun typeCode(code: String) {
        onView(codeInput).waitVisible().perform(replaceText(code))
    }

    fun tapCtaButton() {
        onView(ctaButton).waitVisible().perform(click())
    }
}
