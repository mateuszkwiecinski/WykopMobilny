package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.InfoDialogUi
import io.github.wykopmobilny.ui.base.components.OptionPickerUi
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.Color
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.MessageBodyUi
import io.github.wykopmobilny.ui.components.widgets.TagUi
import io.github.wykopmobilny.ui.components.widgets.ToggleButtonUi
import io.github.wykopmobilny.ui.components.widgets.TwoActionsCounterUi
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi

interface GetLinkDetails : Query<LinkDetailsUi>

class LinkDetailsUi(
    val swipeRefresh: SwipeRefreshUi,
    val header: LinkDetailsHeaderUi,
    val relatedSection: RelatedLinksSectionUi?,
    val contextMenuOptions: List<ContextMenuOptionUi>,
    val commentsSection: CommentsSectionUi,
    val errorDialog: ErrorDialogUi?,
    val infoDialog: InfoDialogUi?,
    val picker: OptionPickerUi?,
    val snackbar: String?,
)

sealed class LinkDetailsHeaderUi {

    object Loading : LinkDetailsHeaderUi()

    data class WithData(
        val title: String,
        val body: String,
        val postedAgo: String,
        val voteCount: TwoActionsCounterUi,
        val commentsCount: Button,
        val upvotePercentage: String?,
        val previewImageUrl: String?,
        val badge: Color?,
        val author: UserInfoUi,
        val currentUser: UserInfoUi?,
        val domain: String,
        val tags: List<TagUi>,
        val favoriteButton: ToggleButtonUi,
        val commentsSort: Button,
        val viewLinkAction: () -> Unit,
        val moreAction: () -> Unit,
        val addCommentAction: () -> Unit,
    ) : LinkDetailsHeaderUi()
}

sealed class RelatedLinksSectionUi {

    object Loading : RelatedLinksSectionUi()

    data class Empty(
        val addLinkAction: () -> Unit,
    ) : RelatedLinksSectionUi()

    data class WithData(
        val links: List<RelatedLinkUi>,
        val addLinkAction: () -> Unit,
    ) : RelatedLinksSectionUi()

    data class FullWidthError(
        val retryAction: () -> Unit,
    ) : RelatedLinksSectionUi()
}

data class RelatedLinkUi(
    val author: UserInfoUi?,
    val upvotesCount: TwoActionsCounterUi,
    val title: String,
    val domain: String,
    val clickAction: () -> Unit,
    val shareAction: () -> Unit,
)

data class CommentsSectionUi(
    val comments: Map<ParentCommentUi, List<LinkCommentUi>>,
    val isLoading: Boolean,
)

data class ParentCommentUi(
    val collapsedCount: String?,
    val toggleExpansionStateAction: (() -> Unit)?,
    val data: LinkCommentUi,
)

sealed class LinkCommentUi {

    data class Hidden(
        val id: Long,
        val author: UserInfoUi,
        val badge: Color?,
        val onClicked: () -> Unit,
    ) : LinkCommentUi()

    data class Normal(
        val id: Long,
        val author: UserInfoUi,
        val postedAgo: String,
        val app: String?,
        val body: MessageBodyUi,
        val badge: Color?,
        val plusCount: Button,
        val minusCount: Button,
        val embed: EmbedMediaUi?,
        val showsOption: Boolean,
        val favoriteButton: ToggleButtonUi,
        val moreAction: () -> Unit,
        val clickAction: () -> Unit,
        val profileAction: () -> Unit = author.avatar.onClicked ?: {},
        val shareAction: () -> Unit,
    ) : LinkCommentUi()
}
