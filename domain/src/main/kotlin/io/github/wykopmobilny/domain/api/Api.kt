package io.github.wykopmobilny.domain.api

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.FetcherResult
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.api.responses.ApiResponse
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.UserSession
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject

internal suspend fun <T : Any> apiCall(
    rawCall: suspend () -> ApiResponse<T>,
    onUnauthorized: (suspend () -> Unit)?,
): FetcherResult<T> {
    val retry = suspend {
        val newResponse = rawCall()
        newResponse.data?.let { FetcherResult.Data(it) }
            ?: newResponse.error?.let { FetcherResult.Error.Message("[${it.code}] ${it.messagePl}") }
            ?: FetcherResult.Error.Message("Invalid response. API's fault")
    }

    return runCatching { rawCall() }
        .mapCatching { response ->
            val data = response.data
            if (data == null) {
                val error = response.error
                if (error == null) {
                    FetcherResult.Error.Message("Invalid response. API's fault")
                } else {
                    when (error.code) {
                        WykopApiCodes.InvalidUserKey, WykopApiCodes.WrongUserSessionKey -> {
                            onUnauthorized?.invoke() ?: error("[${error.code}] ${error.messagePl}")
                            retry()
                        }
                        else -> FetcherResult.Error.Message("[${error.code}] ${error.messagePl}")
                    }
                }
            } else {
                FetcherResult.Data(data)
            }
        }
        .recoverCatching { failure ->
            if (failure is HttpException && failure.code() == 401) {
                onUnauthorized?.invoke() ?: error("[${failure.code()}] ${failure.message()}")
                retry()
            } else {
                FetcherResult.Error.Exception(failure)
            }
        }
        .getOrElse(FetcherResult.Error::Exception)
}

internal class ApiClient @Inject constructor(
    private val userSessionStorage: SessionStorage,
    private val userInfoStore: Store<UserSession, LoggedUserInfo>,
) {

    suspend fun <T : Any> mutation(rawCall: suspend () -> ApiResponse<T>): T {
        val result = apiCall(
            rawCall = { rawCall() },
            onUnauthorized = {
                val session = userSessionStorage.session.first() ?: error("Login required")
                userInfoStore.fresh(session)
            },
        )

        return when (result) {
            is FetcherResult.Data -> result.value
            is FetcherResult.Error.Exception -> throw result.error
            is FetcherResult.Error.Message -> error(result.message)
        }
    }

    fun <TInput : Any, TOutput : Any> fetcher(rawCall: suspend (TInput) -> ApiResponse<TOutput>) =
        Fetcher.ofResult<TInput, TOutput> { args ->
            apiCall(
                rawCall = { rawCall(args) },
                onUnauthorized = {
                    val session = userSessionStorage.session.first() ?: error("Login required")
                    userInfoStore.fresh(session)
                },
            )
        }
}

object WykopApiCodes {
    const val InvalidUserKey = 11
    const val WrongUserSessionKey = 12
}
