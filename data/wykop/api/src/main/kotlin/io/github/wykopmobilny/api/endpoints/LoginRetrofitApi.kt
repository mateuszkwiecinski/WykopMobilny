package io.github.wykopmobilny.api.endpoints

import io.github.wykopmobilny.APP_KEY
import io.github.wykopmobilny.REMOVE_USERKEY_HEADER
import io.github.wykopmobilny.api.responses.LoginResponse
import io.github.wykopmobilny.api.responses.TwoFactorAuthorizationResponse
import io.github.wykopmobilny.api.responses.WykopApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginRetrofitApi {

    @Headers("@: $REMOVE_USERKEY_HEADER")
    @FormUrlEncoded
    @POST("/login/index/appkey/$APP_KEY")
    suspend fun getUserSessionToken(
        @Field("login") login: String,
        @Field("accountkey", encoded = true) accountKey: String,
    ): WykopApiResponse<LoginResponse>

    @FormUrlEncoded
    @POST("/login/2fa/appkey/$APP_KEY")
    suspend fun autorizeWith2FA(@Field("code") code: String): WykopApiResponse<List<TwoFactorAuthorizationResponse>>
}
