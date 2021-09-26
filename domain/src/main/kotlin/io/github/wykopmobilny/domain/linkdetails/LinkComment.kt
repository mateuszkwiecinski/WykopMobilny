package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.profile.UserInfo
import kotlinx.datetime.Instant

internal data class LinkComment(
    val id: Long,
    val app: String?,
    val body: String,
    val postedAt: Instant,
    val author: UserInfo,
    val plusCount: Int,
    val minusCount: Int,
    val userAction: UserVote?
) {

    val totalCount = plusCount - minusCount
}
