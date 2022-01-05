package io.github.wykopmobilny.ui.twofactor

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import io.github.wykopmobilny.ui.base.components.TextInputUi

fun interface GetTwoFactorAuthDetails : Query<TwoFactorAuthDetailsUi>

data class TwoFactorAuthDetailsUi(
    val code: TextInputUi,
    val verifyButton: ProgressButtonUi,
    val onOpenGoogleAuthenticatorClicked: () -> Unit,
    val errorDialog: ErrorDialogUi?,
)
