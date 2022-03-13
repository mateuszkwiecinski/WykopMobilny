package io.github.wykopmobilny.ui.twofactor

import app.cash.paparazzi.Paparazzi
import io.github.wykopmobilny.screenshots.verifyAppState
import io.github.wykopmobilny.ui.twofactor.android.TwoFactorScaffold
import org.junit.Rule
import org.junit.Test

class TwoFactorMainScreenshotTests {

    @get:Rule
    val paparazzi = Paparazzi(maxPercentDifference = 0.01)

    @Test
    fun testAll() = paparazzi.verifyAppState(
        "long" to stubTwoFactorUiLong(),
        "empty_progress" to stubTwoFactorUiEmpty(),
        content = { TwoFactorScaffold(navController = navController, state = state) },
    )
}
