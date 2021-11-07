package io.github.wykopmobilny.domain.linkdetails.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.wykopmobilny.api.responses.LinkCommentResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.LinkCommentsEntity
import io.github.wykopmobilny.data.cache.api.SelectByLinkId
import io.github.wykopmobilny.domain.linkdetails.LinkComment
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.datasource.asUserVote
import io.github.wykopmobilny.domain.profile.datasource.toEntity
import io.github.wykopmobilny.domain.profile.datasource.upsert
import io.github.wykopmobilny.domain.profile.toColorDomain
import io.github.wykopmobilny.domain.profile.toGenderDomain
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

internal fun linkCommentsSourceOfTruth(
    cache: AppCache,
) = SourceOfTruth.of<Long, List<LinkCommentResponse>, Map<LinkComment, List<LinkComment>>>(
    reader = { linkId ->
        cache.linkCommentsQueries.selectByLinkId(linkId)
            .asFlow()
            .mapToList(AppDispatchers.IO)
            .map { comments ->
                val commentsById = comments.asSequence()
                    .filter { it.id == it.parentId }
                    .associateBy { it.id }
                comments
                    .groupBy { commentsById.getValue(it.parentId) }
                    .map { (key, value) ->
                        val parent = key.toContent()
                        val children = value.filterNot { it.id == it.parentId }.map { it.toContent() }

                        parent to children
                    }
                    .toMap()
            }
    },
    writer = { _, comments ->
        cache.transaction {
            comments.forEach { comment ->
                cache.profileQueries.upsert(comment.author)
                comment.embed?.toEntity()?.let(cache.embedQueries::insertOrReplace)
                cache.linkCommentsQueries.insertOrReplace(
                    LinkCommentsEntity(
                        id = comment.id,
                        postedAt = comment.date,
                        profileId = comment.author.login,
                        voteCount = comment.voteCount,
                        voteCountPlus = comment.voteCountPlus,
                        body = comment.body.orEmpty(),
                        parentId = comment.parentId,
                        canVote = comment.canVote,
                        userVote = comment.userVote.asUserVote(),
                        isBlocked = comment.blocked,
                        isFavorite = comment.favorite,
                        linkId = comment.linkId,
                        embedId = comment.embed?.url,
                        app = comment.app,
                        violationUrl = comment.violationUrl,
                    ),
                )
            }
        }
    },
    delete = { linkId -> cache.linkCommentsQueries.deleteByLinkId(linkId) },
)

private fun SelectByLinkId.toContent() =
    LinkComment(
        id = id,
        body = body,
        postedAt = postedAt,
        author = UserInfo(
            profileId = profileId,
            avatarUrl = avatar,
            rank = rank,
            gender = gender?.toGenderDomain(),
            color = color.toColorDomain(),
        ),
        plusCount = voteCountPlus,
        minusCount = (voteCount - voteCountPlus).absoluteValue,
        userAction = userVote,
        app = app,
        embed = embedId?.let {
            Embed(
                id = it,
                type = type!!,
                fileName = fileName,
                preview = preview!!,
                size = size,
                hasAdultContent = hasAdultContent!!,
            )
        },
    )
