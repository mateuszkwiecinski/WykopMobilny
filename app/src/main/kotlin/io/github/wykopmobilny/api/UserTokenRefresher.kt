package io.github.wykopmobilny.api

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.api.errorhandler.WykopExceptionParser
import io.github.wykopmobilny.api.user.LoginApi
import io.github.wykopmobilny.domain.api.WykopApiCodes
import io.github.wykopmobilny.domain.errorhandling.KnownError
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.reactivex.Flowable
import io.reactivex.functions.Function
import kotlinx.coroutines.rx2.rxSingle
import org.reactivestreams.Publisher
import retrofit2.HttpException
import javax.inject.Inject

class UserTokenRefresher @Inject constructor(
    private val userApi: LoginApi,
    private val userManagerApi: UserManagerApi,
    private val errorBodyParser: ErrorBodyParser,
) : Function<Flowable<Throwable>, Publisher<*>> {

    override fun apply(errors: Flowable<Throwable>) = errors.flatMapSingle { failure -> rxSingle { refresh(failure) } }

    private suspend fun refresh(failure: Throwable) {
        when (failure) {
            is WykopExceptionParser.WykopApiException -> {
                when (failure.code) {
                    11, 12 -> saveUserSession()
                    else -> throw failure
                }
            }
            is HttpException -> {
                if (failure.code() == 401) {
                    val errorBody = failure.response()?.errorBody()?.let { errorBody -> errorBodyParser.parse(errorBody) }

                    when (errorBody?.code) {
                        WykopApiCodes.TwoFactorAuthorizationRequired ->
                            throw KnownError.TwoFactorAuthorizationRequired("[${errorBody.code}] ${errorBody.messagePl}")
                        WykopApiCodes.InvalidUserKey,
                        WykopApiCodes.WrongUserSessionKey,
                        -> saveUserSession()
                        else -> {
                            Napier.w(message = "Unsupported error code $errorBody")
                            saveUserSession()
                        }
                    }
                } else {
                    throw failure
                }
            }
            else -> throw failure
        }
    }

    private suspend fun saveUserSession() {
        val token = userApi.getUserSessionToken()
        userManagerApi.saveCredentials(token)
    }
}
