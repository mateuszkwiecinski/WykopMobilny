package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.profile.UserInfo

internal data class RelatedLink(
    val id: Long,
    val url: String,
    val voteCount: Int,
    val author: UserInfo?,
    val title: String,
    val userVote: UserVote?,
)
