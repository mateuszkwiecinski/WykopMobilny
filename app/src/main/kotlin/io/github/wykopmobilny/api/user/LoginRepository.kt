package io.github.wykopmobilny.api.user

import io.github.wykopmobilny.api.endpoints.LoginRetrofitApi
import io.github.wykopmobilny.api.errorhandler.unwrapping
import io.github.wykopmobilny.api.responses.LoginResponse
import io.github.wykopmobilny.storage.api.SessionStorage
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginApi: LoginRetrofitApi,
    private val apiPreferences: SessionStorage,
) : LoginApi {

    override suspend fun getUserSessionToken(): LoginResponse = unwrapping {
        val session = apiPreferences.session.first().let(::checkNotNull)
        loginApi.getUserSessionToken(login = session.login, accountKey = session.token)
    }

}
