package io.github.wykopmobilny.domain.linkdetails.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.github.wykopmobilny.api.responses.LinkCommentResponse
import io.github.wykopmobilny.api.responses.LinkResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.LinkCommentsEntity
import io.github.wykopmobilny.data.cache.api.LinkEntity
import io.github.wykopmobilny.data.cache.api.SelectByLinkId
import io.github.wykopmobilny.domain.linkdetails.LinkComment
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.datasource.asUserVote
import io.github.wykopmobilny.domain.profile.datasource.toEntity
import io.github.wykopmobilny.domain.profile.datasource.upsert
import io.github.wykopmobilny.domain.profile.toColorDomain
import io.github.wykopmobilny.domain.profile.toGenderDomain
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

internal fun linkDetailsSourceOfTruth(
    cache: AppCache,
) = SourceOfTruth.of<Long, LinkResponse, LinkInfo>(
    reader = { linkId ->
        cache.linksQueries.selectById(id = linkId)
            .asFlow()
            .mapToOne(AppDispatchers.IO)
            .filterNotNull()
            .map { link ->
                LinkInfo(
                    id = link.id,
                    title = link.title,
                    description = link.description,
                    tags = link.tags.split(" "),
                    sourceUrl = link.sourceUrl,
                    previewImageUrl = link.previewImageUrl,
                    author = UserInfo(
                        profileId = link.profileId,
                        avatarUrl = link.avatar,
                        rank = link.rank,
                        gender = link.gender?.toGenderDomain(),
                        color = link.color.toColorDomain(),
                    ),
                    commentsCount = link.commentsCount,
                    voteCount = link.voteCount,
                    relatedCount = link.relatedCount,
                    postedAt = link.postedAt,
                    app = link.app,
                    userAction = link.userVote,
                    userFavorite = link.userFavorite,
                )
            }
    },
    writer = { _, link ->
        cache.linksQueries.transaction {
            cache.profileQueries.upsert(link.author)
            cache.linksQueries.insertOrReplace(
                LinkEntity(
                    id = link.id,
                    title = link.title.orEmpty(),
                    description = link.description.orEmpty(),
                    tags = link.tags,
                    sourceUrl = link.sourceUrl,
                    previewImageUrl = link.preview,
                    voteCount = link.voteCount,
                    buryCount = link.buryCount,
                    commentsCount = link.commentsCount,
                    relatedCount = link.relatedCount,
                    postedAt = link.date,
                    plus18 = link.plus18,
                    canVote = link.canVote,
                    isHot = link.isHot,
                    userVote = link.userVote.asUserVote(),
                    userFavorite = link.userFavorite == true,
                    userObserve = link.userObserve == true,
                    app = link.app,
                    profileId = link.author.login,
                ),
            )
        }
    },
    delete = { linkId -> cache.linksQueries.deleteById(linkId) },
)

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
        cache.linkCommentsQueries.transaction {
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
    )
