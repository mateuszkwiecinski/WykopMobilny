package io.github.wykopmobilny.domain.repositories

import io.github.wykopmobilny.api.endpoints.TagRetrofitApi
import io.github.wykopmobilny.api.responses.ObserveStateResponse
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.api.ApiClient
import javax.inject.Inject

internal class TagsRepository @Inject constructor(
    private val api: ApiClient,
    private val tagsApi: TagRetrofitApi,
    private val appStorage: AppStorage,
) {

    suspend fun blockTag(tag: String): ObserveStateResponse {
        val response = api.mutation { tagsApi.block(tag) }
        appStorage.blacklistQueries.insertOrReplaceTag(tagId = tag)

        return response
    }

    suspend fun unblockTag(tag: String): ObserveStateResponse {
        val response = api.mutation { tagsApi.unblock(tag) }
        appStorage.blacklistQueries.deleteTag(tagId = tag)

        return response
    }
}
