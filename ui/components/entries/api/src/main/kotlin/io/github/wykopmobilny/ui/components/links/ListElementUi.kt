package io.github.wykopmobilny.ui.components.links

import io.github.wykopmobilny.ui.components.users.Color
import io.github.wykopmobilny.ui.components.users.UserInfoUi

sealed class ListElementUi {

    data class Entry(
        val id: Long,
        val body: String,
        val voteCount: UpvoteCounter,
        val previewImageUrl: String?,
        val commentsCount: Int,
        val author: UserInfoUi,
        val addedAgo: String,
        val app: String?,
        val hasPlus18Overlay: Boolean,
        val isFavorite: Boolean,
        val shareAction: () -> Unit,
        val favoriteAction: () -> Unit,
        val voteAction: () -> Unit,
        val moreAction: (() -> Unit)?,
    ) : ListElementUi()


    data class Link(
        val id: Long,
        val title: String,
        val body: String,
        val previewImageUrl: String?,
        val commentsCount: Int,
        val voteCount: UpvoteCounter,
        val addedAgo: String,
        val thumbnail: Thumbnail,
        val shareAction: () -> Unit,
        val favoriteAction: () -> Unit,
        val voteAction: () -> Unit,
        val moreAction: (() -> Unit)?,
    ) : ListElementUi() {

        enum class Thumbnail {
            None,
            SmallOnLeft,
            SmallOnRight,
            LargeOnTop,
            LargeOnBottom,
        }
    }
}

val ListElementUi.id
    get() = when (this) {
        is ListElementUi.Entry -> id
        is ListElementUi.Link -> id
    }

data class UpvoteCounter(
    val count: Int,
    val color: Color,
)
