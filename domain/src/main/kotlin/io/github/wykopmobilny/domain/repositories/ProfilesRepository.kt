package io.github.wykopmobilny.domain.repositories

import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.responses.ObserveStateResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.api.ApiClient
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ProfilesRepository @Inject constructor(
    private val api: ApiClient,
    private val profilesApi: ProfileRetrofitApi,
    private val appCache: AppCache,
    private val appStorage: AppStorage,
) {

    suspend fun blockUser(profileId: String) {
        val response = api.mutation { profilesApi.block(profileId) }
        update(profileId = profileId, response = response)
    }

    suspend fun unblockUser(profileId: String) {
        val response = api.mutation { profilesApi.unblock(profileId) }
        update(profileId = profileId, response = response)
    }

    suspend fun observeUser(profileId: String) {
        val response = api.mutation { profilesApi.observe(profileId) }
        update(profileId = profileId, response = response)
    }

    suspend fun unobserveUser(profileId: String) {
        val response = api.mutation { profilesApi.unobserve(profileId) }
        update(profileId = profileId, response = response)
    }

    private suspend fun update(profileId: String, response: ObserveStateResponse) = withContext(AppDispatchers.IO) {
        launch {
            appStorage.blacklistQueries.run {
                if (response.isBlocked) {
                    insertOrReplaceProfile(profileId)
                } else {
                    deleteProfile(profileId)
                }
            }
        }
        appCache.profileStateQueries.transaction {
            if (response.isBlocked) {
                appCache.profileStateQueries.blockProfile(profileId)
            } else {
                appCache.profileStateQueries.unblockProfile(profileId)
            }
            if (response.isObserved) {
                appCache.profileStateQueries.observeProfile(profileId)
            } else {
                appCache.profileStateQueries.unobserveProfile(profileId)
            }
        }
    }
}
