package io.github.wykopmobilny.domain.blacklist.actions

import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.responses.ObserveStateResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.domain.api.ApiClient
import io.github.wykopmobilny.storage.api.BlacklistPreferencesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class UsersRepository @Inject constructor(
    private val api: ApiClient,
    private val usersApi: ProfileRetrofitApi,
    private val appCache: AppCache,
    private val blacklistPreferencesApi: BlacklistPreferencesApi,
) {

    suspend fun blockUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.block(profileId) }
        update(profileId = profileId, response = response)

        return response
    }

    suspend fun unblockUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.unblock(profileId) }
        update(profileId = profileId, response = response)

        return response
    }

    suspend fun observeUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.observe(profileId) }
        update(profileId = profileId, response = response)

        return response
    }

    suspend fun unobserveUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.unobserve(profileId) }
        update(profileId = profileId, response = response)

        return response
    }

    private suspend fun update(profileId: String, response: ObserveStateResponse) = coroutineScope {
        launch {
            blacklistPreferencesApi.update {
                if (response.isBlocked) {
                    it.copy(users = it.users + profileId)
                } else {
                    it.copy(users = it.users - profileId)
                }
            }
        }
        appCache.profilesQueries.updateStatus(
            id = profileId,
            isObserved = response.isObserved,
            isBlocked = response.isBlocked,
        )
    }
}
