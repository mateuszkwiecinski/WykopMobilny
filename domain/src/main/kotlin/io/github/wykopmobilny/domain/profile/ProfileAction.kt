package io.github.wykopmobilny.domain.profile

import io.github.wykopmobilny.data.cache.api.UserVote
import kotlinx.datetime.Instant

internal sealed class ProfileAction {

    data class Link(
        val id: Long,
        val title: String,
        val description: String,
        val previewImageUrl: String?,
        val commentsCount: Int,
        val voteCount: Int,
        val postedAt: Instant,
        val userAction: UserVote?,
        val app: String?,
        val author: UserInfo,
    ) : ProfileAction()

    data class Entry(
        val id: Long,
        val postedAt: Instant,
        val body: String,
        val voteCount: Int,
        val previewImageUrl: String?,
        val commentsCount: Int,
        val author: UserInfo,
        val userAction: UserVote?,
        val isFavorite: Boolean,
        val app: String?,
    ) : ProfileAction()
}

internal data class UserInfo(
    val profileId: String,
    val avatarUrl: String,
    val rank: Int?,
    val gender: Gender?,
    val color: Color,
) {

    enum class Gender {
        Male,
        Female,
    }

    enum class Color {
        Green,
        Orange,
        Claret,
        Admin,
        Banned,
        Deleted,
        Client,
        Unknown,
    }
}
