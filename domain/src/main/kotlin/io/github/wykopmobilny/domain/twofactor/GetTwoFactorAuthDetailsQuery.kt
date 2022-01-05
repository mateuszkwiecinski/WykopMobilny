package io.github.wykopmobilny.domain.twofactor

import io.github.wykopmobilny.api.endpoints.LoginRetrofitApi
import io.github.wykopmobilny.domain.api.ApiClient
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.twofactor.di.TwoFactorAuthScope
import io.github.wykopmobilny.domain.utils.safe
import io.github.wykopmobilny.domain.utils.withResource
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.Resource
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import io.github.wykopmobilny.ui.base.components.TextInputUi
import io.github.wykopmobilny.ui.twofactor.GetTwoFactorAuthDetails
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDetailsUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetTwoFactorAuthDetailsQuery @Inject constructor(
    private val appScopes: AppScopes,
    private val viewStateStorage: TwoFactorAuthViewStateStorage,
    private val api: ApiClient,
    private val loginApi: LoginRetrofitApi,
    private val interopRequestsProvider: InteropRequestsProvider,
    private val appRestarter: AppRestarter,
) : GetTwoFactorAuthDetails {

    override fun invoke() =
        viewStateStorage.state.map { viewState ->
            TwoFactorAuthDetailsUi(
                code = TextInputUi(
                    text = viewState.code,
                    onChanged = safeCallback { updated -> viewStateStorage.update { it.copy(code = updated) } },
                ),
                verifyButton = if (viewState.generalResource.isLoading) {
                    ProgressButtonUi.Loading
                } else {
                    ProgressButtonUi.Default(
                        onClicked = safeCallback {
                            withResource(
                                refresh = { send2FACode(viewState.code) },
                                update = { resource -> viewStateStorage.update { it.copy(generalResource = resource) } },
                                launch = { callback -> appScopes.safe<TwoFactorAuthScope>(block = callback) },
                            )
                        },
                    )
                },
                onOpenGoogleAuthenticatorClicked = safeCallback { interopRequestsProvider.request(InteropRequest.OpenGoogleAuthenticator) },
                errorDialog = viewState.generalResource.failedAction?.let { error ->
                    ErrorDialogUi(
                        error = error.cause,
                        retryAction = error.retryAction,
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(generalResource = Resource.idle()) } },
                    )
                },
            )
        }

    private suspend fun send2FACode(code: String) {
        api.mutation { loginApi.autorizeWith2FA(code) }
        appRestarter.restart()
    }

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safe<TwoFactorAuthScope> {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(generalResource = Resource.error(FailedAction(failure))) } }
        }
    }

    private fun <T> safeCallback(function: suspend CoroutineScope.(T) -> Unit): (T) -> Unit = {
        appScopes.safe<TwoFactorAuthScope> {
            runCatching { function(it) }
                .onFailure { failure -> viewStateStorage.update { it.copy(generalResource = Resource.error(FailedAction(failure))) } }
        }
    }
}
