package io.github.wykopmobilny.api.user

import io.github.wykopmobilny.api.responses.LoginResponse

interface LoginApi {
    suspend fun getUserSessionToken(): LoginResponse
}
