package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.entries.FilteredData
import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.responses.EntryResponse
import io.github.wykopmobilny.models.dataclass.Entry

fun EntryResponse.filterEntry(owmContentFilter: OWMContentFilter) =
    owmContentFilter.filterEntry(
        Entry(
            id = id,
            author = author.let(AuthorMapper::map),
            body = body.orEmpty(),
            fullDate = date,
            isVoted = userVote > 0,
            isFavorite = favorite,
            survey = survey?.let(SurveyMapper::map),
            embed = embed?.let(EmbedMapper::map),
            voteCount = voteCount,
            commentsCount = commentsCount,
            comments = comments.orEmpty().map { EntryCommentMapper.map(it, owmContentFilter) }.toMutableList(),
            app = app,
            violationUrl = violationUrl,
            isNsfw = body?.lowercase()?.contains("#nsfw") == true,
            isBlocked = blocked,
            collapsed = true,
            isCommentingPossible = isCommentingPossible == true,
        ),
    )

fun List<EntryResponse>.filterEntries(owmContentFilter: OWMContentFilter) =
    FilteredData(
        totalCount = size,
        filtered = map { response -> response.filterEntry(owmContentFilter) },
    )
