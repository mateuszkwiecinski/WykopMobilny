package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.components.widgets.ColoredCounterUi
import io.github.wykopmobilny.ui.components.widgets.PlainCounterUi
import io.github.wykopmobilny.ui.components.widgets.TagUi
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi

interface GetLinkDetails : Query<LinkDetailsUi>

class LinkDetailsUi(
    val header: LinkDetailsHeaderUi,
    val commentsSection: CommentsSectionUi,
    val errorDialog: ErrorDialogUi?,
)

sealed class LinkDetailsHeaderUi {

    object Loading : LinkDetailsHeaderUi()

    data class WithData(
        val title: String,
        val body: String,
        val postedAgo: String,
        val voteCount: ColoredCounterUi,
        val previewImageUrl: String?,
        val commentsCount: PlainCounterUi,
        val relatedLinksCount: PlainCounterUi,
        val isFavorite: Boolean,
        val author: UserInfoUi,
        val sourceUrl: String?,
        val tags: List<TagUi>,
        val onAuthorClicked: () -> Unit,
        val refreshAction: () -> Unit,
        val onClicked: () -> Unit,
        val shareAction: () -> Unit,
        val favoriteAction: () -> Unit,
    ) : LinkDetailsHeaderUi()
}

data class CommentsSectionUi(
    val comments: Map<LinkCommentUi, List<LinkCommentUi>>,
    val isLoading: Boolean,
)

data class LinkCommentUi(
    val author: UserInfoUi,
    val postedAgo: String,
    val app: String?,
    val body: String,
    val plusCount: ColoredCounterUi,
    val minusCount: ColoredCounterUi,
    val shareAction: () -> Unit,
    val favoriteAction: () -> Unit,
)
