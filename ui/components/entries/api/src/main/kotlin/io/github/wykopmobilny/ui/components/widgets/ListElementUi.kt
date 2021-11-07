package io.github.wykopmobilny.ui.components.widgets

sealed class ListElementUi {

    data class Entry(
        val id: Long,
        val body: String,
        val voteCount: Button,
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
    ) : ListElementUi()

    data class Link(
        val id: Long,
        val title: String,
        val body: String,
        val previewImageUrl: String?,
        val commentsCount: Int,
        val voteCount: Button,
        val addedAgo: String,
        val thumbnail: Thumbnail,
        val shareAction: () -> Unit,
        val favoriteAction: () -> Unit,
        val voteAction: () -> Unit,
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
