package io.github.wykopmobilny.domain.blacklist.actions

import com.dropbox.android.external.store4.Store
import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.responses.ObserveStateResponse
import io.github.wykopmobilny.api.responses.ProfileResponse
import io.github.wykopmobilny.domain.api.ApiClient
import io.github.wykopmobilny.storage.api.BlacklistPreferencesApi
import javax.inject.Inject

internal class UsersRepository @Inject constructor(
    private val api: ApiClient,
    private val usersApi: ProfileRetrofitApi,
    private val store: Store<String, ProfileResponse>,
    private val blacklistPreferencesApi: BlacklistPreferencesApi,
) {

    suspend fun blockUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.block(profileId) }
        blacklistPreferencesApi.update(profileId, response.isBlocked)

        return response
    }

    suspend fun unblockUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.unblock(profileId) }
        blacklistPreferencesApi.update(profileId, response.isBlocked)

        return response
    }

    suspend fun observeUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.observe(profileId) }
        blacklistPreferencesApi.update(profileId, response.isBlocked)

        return response
    }

    suspend fun unobserveUser(profileId: String): ObserveStateResponse {
        val response = api.fetch { usersApi.unobserve(profileId) }
        blacklistPreferencesApi.update(profileId, response.isBlocked)

        return response
    }
}

private suspend fun BlacklistPreferencesApi.update(profileId: String, isBlocked: Boolean) {
    update {
        if (isBlocked) {
            it.copy(users = it.users + profileId)
        } else {
            it.copy(users = it.users - profileId)
        }
    }
}
