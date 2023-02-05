package io.github.wykopmobilny.domain.repositories

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.api.endpoints.LinksRetrofitApi
import io.github.wykopmobilny.api.responses.DigResponse
import io.github.wykopmobilny.api.responses.LinkVoteResponse
import io.github.wykopmobilny.api.responses.VoteResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.api.ApiClient
import io.github.wykopmobilny.domain.linkdetails.VoteDownReason
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LinksRepository @Inject constructor(
    private val api: ApiClient,
    private val linkStore: Store<Long, LinkInfo>,
    private val linksApi: LinksRetrofitApi,
    private val appCache: AppCache,
) {

    suspend fun toggleFavorite(linkId: Long) {
        api.mutation { linksApi.toggleFavorite(linkId) }
        linkStore.fresh(linkId)
    }

    suspend fun toggleCommentFavorite(linkId: Long, commentId: Long) {
        val response = api.mutation { linksApi.toggleCommentFavorite(commentId) }
        withContext(AppDispatchers.IO) {
            appCache.linkCommentsQueries.favorite(
                linkId = linkId,
                id = commentId,
                isFavorite = response.isFavorited,
            )
        }
    }

    suspend fun voteUp(linkId: Long) {
        val response = api.mutation { linksApi.voteUp(linkId) }
        updateLinkVotes(linkId, response, userVote = UserVote.Up)
    }

    suspend fun removeVote(linkId: Long) {
        val response = api.mutation { linksApi.voteRemove(linkId) }
        updateLinkVotes(linkId, response, userVote = null)
    }

    suspend fun voteDown(linkId: Long, reason: VoteDownReason) {
        val response = api.mutation { linksApi.voteDown(linkId, reason.apiValue) }
        updateLinkVotes(linkId, response, userVote = UserVote.Down)
    }

    suspend fun commentVoteUp(linkId: Long, commentId: Long) {
        val response = api.mutation { linksApi.commentVoteUp(linkId = linkId, commentId = commentId) }
        updateCommentVotes(
            linkId = linkId,
            commentId = commentId,
            response = response,
            userVote = UserVote.Up,
        )
    }

    suspend fun commentVoteDown(linkId: Long, commentId: Long) {
        val response = api.mutation { linksApi.commentVoteDown(linkId = linkId, commentId = commentId) }
        updateCommentVotes(
            linkId = linkId,
            commentId = commentId,
            response = response,
            userVote = UserVote.Down,
        )
    }

    suspend fun removeCommentVote(linkId: Long, commentId: Long) {
        val response = api.mutation { linksApi.commentVoteCancel(linkId = linkId, commentId = commentId) }
        updateCommentVotes(
            linkId = linkId,
            commentId = commentId,
            response = response,
            userVote = null,
        )
    }

    suspend fun relatedVoteUp(linkId: Long, relatedId: Long) {
        val response = api.mutation { linksApi.relatedVoteDown(linkId = linkId, relatedId = relatedId) }
        updateRelatedLinks(
            linkId = linkId,
            relatedId = relatedId,
            response = response,
            userVote = UserVote.Up,
        )
    }

    suspend fun relatedVoteDown(linkId: Long, relatedId: Long) {
        val response = api.mutation { linksApi.relatedVoteDown(linkId = linkId, relatedId = relatedId) }
        updateRelatedLinks(
            linkId = linkId,
            relatedId = relatedId,
            response = response,
            userVote = UserVote.Down,
        )
    }

    private suspend fun updateLinkVotes(
        linkId: Long,
        response: DigResponse,
        userVote: UserVote?,
    ) = withContext(AppDispatchers.IO) {
        appCache.linksQueries.vote(
            id = linkId,
            voteCount = response.diggs,
            buryCount = response.buries,
            userVote = userVote,
        )
    }

    private suspend fun updateCommentVotes(
        linkId: Long,
        commentId: Long,
        response: LinkVoteResponse,
        userVote: UserVote?,
    ) = withContext(AppDispatchers.IO) {
        appCache.linkCommentsQueries.vote(
            id = commentId,
            linkId = linkId,
            userVote = userVote,
            voteCount = response.voteCount,
            voteCountPlus = response.voteCountPlus,
        )
    }

    private suspend fun updateRelatedLinks(
        linkId: Long,
        relatedId: Long,
        response: VoteResponse,
        userVote: UserVote,
    ) = withContext(AppDispatchers.IO) {
        val voteCount = response.voteCount
        if (voteCount != null) {
            appCache.linksRelatedQueries.vote(
                id = relatedId,
                linkId = linkId,
                userVote = userVote,
                voteCount = voteCount,
            )
        }
    }
}

private val VoteDownReason.apiValue: Int
    get() = when (this) {
        VoteDownReason.Duplicate -> 1
        VoteDownReason.Spam -> 2
        VoteDownReason.FakeInfo -> 3
        VoteDownReason.WrongContent -> 4
        VoteDownReason.UnsuitableContent -> 5
    }
