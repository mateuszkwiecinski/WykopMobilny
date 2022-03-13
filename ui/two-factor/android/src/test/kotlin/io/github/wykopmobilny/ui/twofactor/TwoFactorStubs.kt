package io.github.wykopmobilny.ui.twofactor

import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import io.github.wykopmobilny.ui.base.components.TextInputUi

fun stubTwoFactorUiLong() = TwoFactorAuthDetailsUi(
    code = TextInputUi(
        text = "123456",
        onChanged = {},
    ),
    verifyButton = ProgressButtonUi.Default(
        label = "Weryfikuj",
        onClicked = {},
    ),
    authenticatorButton = ProgressButtonUi.Default(
        label = "Very long Authenticator app",
        onClicked = {},
    ),
    errorDialog = null,
)

fun stubTwoFactorUiEmpty() = TwoFactorAuthDetailsUi(
    code = TextInputUi(
        text = "",
        onChanged = {},
    ),
    verifyButton = ProgressButtonUi.Loading,
    authenticatorButton = ProgressButtonUi.Default(
        label = "Short",
        onClicked = {},
    ),
    errorDialog = null,
)
