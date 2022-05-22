package io.github.wykopmobilny.tests.pages

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible

class TwoFactorAuthPage(private val rule: ComposeTestRule) : Page {

    private val title = hasText("Wygląda na to że Twoje konto korzysta z uwierzytenienia dwuskładnikowego (2FA)", substring = true)
    private val codeInput = hasText("6 cyfrowy kod")
    private val ctaButton = hasText("Weryfikuj")

    fun assertVisible() {
        rule.onNode(title).waitVisible()
    }

    fun typeCode(code: String) {
        rule.onNode(codeInput).waitVisible().performTextInput(code)
    }

    fun tapCtaButton() {
        rule.onNode(ctaButton).waitVisible().performClick()
    }
}
