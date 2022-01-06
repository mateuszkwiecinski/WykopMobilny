package io.github.wykopmobilny.domain.twofactor

import io.github.wykopmobilny.domain.twofactor.di.TwoFactorAuthScope
import io.github.wykopmobilny.ui.base.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@TwoFactorAuthScope
internal class TwoFactorAuthViewStateStorage @Inject constructor() {

    val state = MutableStateFlow(value = TwoFactorAuthViewState())

    fun update(updater: (TwoFactorAuthViewState) -> TwoFactorAuthViewState) {
        state.update(updater)
    }
}

data class TwoFactorAuthViewState(
    val generalResource: Resource = Resource.idle(),
    val code: String = "",
)
