package io.github.wykopmobilny.domain.api

import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class StoreMediator<T : Any> @Inject constructor(
    private val store: Store<Int, List<T>>,
) : RemoteMediator<Int, T>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, T>): MediatorResult {
        val loadKey = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> state.pages.firstOrNull()?.prevKey
            LoadType.APPEND -> state.pages.lastOrNull()?.nextKey
        } ?: return MediatorResult.Success(endOfPaginationReached = true)
        val response = store.stream(StoreRequest.fresh(loadKey))
            .first {
                when (it) {
                    is StoreResponse.Loading -> false
                    is StoreResponse.Data,
                    is StoreResponse.NoNewData,
                    is StoreResponse.Error,
                    -> true
                }
            }

        return when (response) {
            is StoreResponse.Loading -> error("excluded")
            is StoreResponse.Data -> MediatorResult.Success(endOfPaginationReached = response.value.isEmpty())
            is StoreResponse.NoNewData -> MediatorResult.Success(endOfPaginationReached = true)
            is StoreResponse.Error.Exception -> MediatorResult.Error(response.error)
            is StoreResponse.Error.Message -> MediatorResult.Error(Exception(response.message))
        }
    }
}

internal class PagingSource<T : Any> @Inject constructor(
    private val store: Store<Int, List<T>>,
) : PagingSource<Int, T>() {

    private val job = Job()
    private val scope = CoroutineScope(job + AppDispatchers.Default)

    init {
        registerInvalidatedCallback { job.cancelChildren() }
    }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, T> = withContext(AppDispatchers.Default) {
        val nextPageNumber = params.key ?: 1
        runCatching {
            val response = store.stream(StoreRequest.cached(nextPageNumber, refresh = false))
                .mapNotNull { it.dataOrNull() }
            val page = response.first()

            scope.launch {
                response.first { it != page }
                invalidate()
            }

            LoadResult.Page(
                data = page,
                prevKey = null,
                nextKey = if (page.isNotEmpty()) nextPageNumber + 1 else null,
            )
        }
            .getOrElse { LoadResult.Error(it) }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let(state::closestPageToPosition)?.let { anchorPage ->
            anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
        }
}
