package io.github.wykopmobilny.domain.login

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.domain.login.di.LoginScope
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.domain.utils.safe
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.UserSession
import io.github.wykopmobilny.kotlin.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.login.Login
import io.github.wykopmobilny.ui.login.LoginUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginQuery @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val userInfoStore: Store<UserSession, LoggedUserInfo>,
    private val blacklistStore: Store<Unit, Blacklist>,
    private val loginConfig: () -> ConnectConfig,
    private val appRestarter: AppRestarter,
    private val viewStateStorage: SimpleViewStateStorage,
    private val appScopes: AppScopes,
) : Login {

    override fun invoke() =
        viewStateStorage.state.map { viewState ->
            LoginUi(
                urlToLoad = loginConfig().connectUrl,
                isLoading = viewState.isLoading,
                errorDialog = viewState.failedAction?.let { failure ->
                    ErrorDialogUi(
                        error = failure.cause,
                        retryAction = failure.retryAction,
                        dismissAction = { appScopes.safe<LoginScope> { viewStateStorage.update { it.copy(failedAction = null) } } },
                    )
                },
                parseUrlAction = ::onUrlInvoked,
            )
        }

    private fun onUrlInvoked(url: String) = appScopes.safe<LoginScope> {
        val userSession = withContext(Dispatchers.Default) {
            val match = loginPattern.find(url) ?: return@withContext null

            val login = match.groups[1]?.value?.takeIf { it.isNotBlank() }
            val token = match.groups[2]?.value?.takeIf { it.isNotBlank() }

            if (login.isNullOrBlank() || token.isNullOrBlank()) {
                null
            } else {
                UserSession(login, token)
            }
        } ?: return@safe
        viewStateStorage.update { it.copy(isLoading = true) }

        runCatching {
            sessionStorage.updateSession(userSession)
            userInfoStore.fresh(userSession)
            blacklistStore.fresh(Unit)
            appRestarter.restart()
        }
            .onFailure { throwable ->
                sessionStorage.updateSession(null)
                userInfoStore.clearAll()
                blacklistStore.clearAll()
                viewStateStorage.update { it.copy(isLoading = false, failedAction = FailedAction(cause = throwable)) }
            }
            .onSuccess { viewStateStorage.update { it.copy(isLoading = false, failedAction = null) } }
    }

    companion object {
        private val loginPattern = "/ConnectSuccess/appkey/.+/login/(.+)/token/(.+)/".toRegex()
    }
}
