package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.responses.LinkCommentResponse
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.utils.toPrettyDate

object LinkCommentMapper {

    fun map(value: LinkCommentResponse, owmContentFilter: OWMContentFilter) =
        owmContentFilter.filterLinkComment(
            LinkComment(
                id = value.id,
                author = AuthorMapper.map(value.author),
                fullDate = value.date.toPrettyDate(),
                body = value.body,
                favorite = value.favorite,
                voteCount = value.voteCount,
                voteCountPlus = value.voteCountPlus,
                voteCountMinus = value.voteCount - value.voteCountPlus,
                userVote = value.userVote,
                parentId = value.parentId,
                canVote = value.canVote,
                linkId = value.linkId,
                embed = value.embed?.let(EmbedMapper::map),
                app = value.app,
                isCollapsed = false,
                isParentCollapsed = false,
                childCommentCount = 0,
                violationUrl = value.violationUrl,
                isNsfw = value.body?.lowercase()?.contains("#nsfw") ?: false,
                isBlocked = value.blocked,
            ),
        )
}
