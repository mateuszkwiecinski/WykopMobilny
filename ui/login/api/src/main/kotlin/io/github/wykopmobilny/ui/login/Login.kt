package io.github.wykopmobilny.ui.login

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi

interface Login : Query<LoginUi>

data class LoginUi(
    val urlToLoad: String,
    val isLoading: Boolean,
    val errorDialog: ErrorDialogUi?,
    val parseUrlAction: (String) -> Unit,
)
