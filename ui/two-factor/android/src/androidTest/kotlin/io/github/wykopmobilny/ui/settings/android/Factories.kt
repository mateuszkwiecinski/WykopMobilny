package io.github.wykopmobilny.ui.settings.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.ui.twofactor.GetTwoFactorAuthDetails
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDetailsUi
import kotlinx.coroutines.flow.flowOf

internal fun BaseScreenshotTest.registerTwoFactor(
    twoFactorUi: () -> TwoFactorAuthDetailsUi = { error("unsupported") },
) = registerDependencies<TwoFactorAuthDependencies>(
    object : TwoFactorAuthDependencies {
        override fun getTwoFactorAuthDetails() = GetTwoFactorAuthDetails { flowOf(twoFactorUi()) }
    },
)
