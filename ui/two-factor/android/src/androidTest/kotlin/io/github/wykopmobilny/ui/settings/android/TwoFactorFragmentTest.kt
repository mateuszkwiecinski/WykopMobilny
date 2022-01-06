package io.github.wykopmobilny.ui.settings.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.unboundedHeight
import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import io.github.wykopmobilny.ui.base.components.TextInputUi
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDetailsUi
import io.github.wykopmobilny.ui.twofactor.android.TwoFactorMainFragment
import org.junit.Test

internal class TwoFactorFragmentTest : BaseScreenshotTest() {

    override fun createFragment() = TwoFactorMainFragment()

    @Test
    fun defaultState() {
        registerTwoFactor(
            twoFactorUi = {
                TwoFactorAuthDetailsUi(
                    code = TextInputUi(
                        text = "123456",
                        onChanged = {},
                    ),
                    verifyButton = ProgressButtonUi.Default(
                        onClicked = {},
                    ),
                    onOpenGoogleAuthenticatorClicked = {},
                    errorDialog = null,
                )
            },
        )
        record(size = unboundedHeight())
    }

    @Test
    fun withProgress() {
        registerTwoFactor(
            twoFactorUi = {
                TwoFactorAuthDetailsUi(
                    code = TextInputUi(
                        text = "",
                        onChanged = {},
                    ),
                    verifyButton = ProgressButtonUi.Loading,
                    onOpenGoogleAuthenticatorClicked = {},
                    errorDialog = null,
                )
            },
        )
        record(size = unboundedHeight())
    }
}
