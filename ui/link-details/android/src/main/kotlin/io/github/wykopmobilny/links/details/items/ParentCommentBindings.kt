package io.github.wykopmobilny.links.details.items

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.bind
import com.github.wykopmobilny.ui.components.setUserNick
import com.github.wykopmobilny.ui.components.toColorInt
import com.github.wykopmobilny.ui.components.utils.bind
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.links.details.ParentCommentUi
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentHiddenBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.setOnLongClick
import androidx.appcompat.R as AppcompatR
import io.github.wykopmobilny.ui.base.android.R as BaseR

internal fun LinkDetailsParentCommentBinding.bindParentComment(
    parent: ParentCommentUi,
    data: LinkCommentUi.Normal,
    hasReplies: Boolean,
) {
    clickableContainer.setOnClick(data.clickAction)
    clickableContainer.setOnLongClick(parent.toggleExpansionStateAction)
    if (data.showsOption) {
        clickableContainer.setBackgroundColor(clickableContainer.context.readColorAttr(AppcompatR.attr.colorControlHighlight).defaultColor)
    } else {
        clickableContainer.background = null
    }

    txtCollapsed.isVisible = parent.collapsedCount != null
    txtCollapsed.text = parent.collapsedCount
    txtCollapsed.setOnClick(parent.toggleExpansionStateAction)

    imgAvatar.bind(data.author.avatar)
    txtUser.setUserNick(data.author)
    txtTimestamp.text = data.postedAgo
    txtApp.text = data.app?.let { "via $it" }
    txtBody.bind(data.body)
    imgEmbed.bind(data.embed)
    imgBadge.setBackgroundColor(data.badge.toColorInt(context = root.context).defaultColor)
    plusButton.bind(data.plusCount)
    minusButton.bind(data.minusCount)
    btnMore.setOnClick(data.moreAction)
    lineComment.isVisible = hasReplies
    optionsContainer.isVisible = data.showsOption
    btnProfile.setOnClick(data.profileAction)
    btnShare.setOnClick(data.shareAction)
    btnFavorite.setOnClick(data.favoriteButton.clickAction)
    btnFavorite.setImageResource(if (data.favoriteButton.isToggled) BaseR.drawable.ic_favorite else BaseR.drawable.ic_favorite_outlined)
    btnFavorite.imageTintList = if (data.favoriteButton.isToggled) {
        ColorStateList.valueOf(ContextCompat.getColor(btnFavorite.context, BaseR.color.favorite_enabled))
    } else {
        btnFavorite.context.readColorAttr(AppcompatR.attr.colorControlNormal)
    }
    btnReply.setOnClick(data.profileAction)
}

internal fun LinkDetailsParentCommentHiddenBinding.bindHiddenParent(
    parent: ParentCommentUi,
    data: LinkCommentUi.Hidden,
) {
    root.setOnClick(data.onClicked)
    root.setOnLongClick(parent.toggleExpansionStateAction)

    txtCollapsed.isVisible = parent.collapsedCount != null
    txtCollapsed.text = parent.collapsedCount
    txtCollapsed.setOnClick(parent.toggleExpansionStateAction)

    txtUser.setUserNick(data.author)
    imgBadge.setBackgroundColor(data.badge.toColorInt(context = root.context).defaultColor)
}
