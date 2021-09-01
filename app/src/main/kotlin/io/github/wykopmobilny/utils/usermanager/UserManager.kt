package io.github.wykopmobilny.utils.usermanager

import android.content.Context
import io.github.wykopmobilny.api.responses.LoginResponse
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.dialogs.userNotLoggedInDialog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class UserCredentials(
    val login: String,
    val avatarUrl: String,
    val backgroundUrl: String?,
    val userKey: String,
)

interface SimpleUserManagerApi {
    fun getUserCredentials(): UserCredentials?
}

interface UserManagerApi : SimpleUserManagerApi {
    suspend fun logoutUser()
    suspend fun saveCredentials(credentials: LoginResponse)
    fun runIfLoggedIn(context: Context, callback: () -> Unit)
}

fun UserManagerApi.isUserAuthorized() = getUserCredentials() != null

@Singleton
class UserManager @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val userInfoStorage: UserInfoStorage,
    private val appScopes: AppScopes,
) : UserManagerApi {

    private val userInfo = userInfoStorage.loggedUser
        .stateIn(appScopes.applicationScope, SharingStarted.Eagerly, null)

    override suspend fun logoutUser() {
        sessionStorage.updateSession(null)
        userInfoStorage.updateLoggedUser(null)
        userInfo.first { it == null }
    }

    override suspend fun saveCredentials(credentials: LoginResponse) {
        userInfoStorage.updateLoggedUser(
            value = LoggedUserInfo(
                id = credentials.profile.id,
                userToken = credentials.userkey,
                avatarUrl = credentials.profile.avatar,
                backgroundUrl = credentials.profile.background,
            ),
        )
    }

    override fun getUserCredentials(): UserCredentials? =
        userInfo.value?.let {
            UserCredentials(
                login = it.id,
                avatarUrl = it.avatarUrl,
                backgroundUrl = it.backgroundUrl,
                userKey = it.userToken,
            )
        }

    override fun runIfLoggedIn(context: Context, callback: () -> Unit) {
        appScopes.applicationScope.launch {
            val isLoggedIn = sessionStorage.session.first()
            if (isLoggedIn != null) {
                callback.invoke()
            } else {
                withContext(AppDispatchers.Main) {
                    userNotLoggedInDialog(context)?.show()
                }
            }
        }
    }
}
