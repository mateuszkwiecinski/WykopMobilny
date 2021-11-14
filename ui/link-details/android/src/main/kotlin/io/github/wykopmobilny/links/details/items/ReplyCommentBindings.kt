package io.github.wykopmobilny.links.details.items

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.bind
import com.github.wykopmobilny.ui.components.setUserNick
import com.github.wykopmobilny.ui.components.utils.bind
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentHiddenBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

internal fun LinkDetailsReplyCommentHiddenBinding.bindHiddenReply(comment: LinkCommentUi.Hidden) {
    root.setOnClick(comment.onClicked)
    txtUser.setUserNick(comment.author)
    val badgeColor = (comment.badge?.toColorInt(context = root.context) ?: root.context.readColorAttr(R.attr.colorDivider)).defaultColor
    imgBadge.setBackgroundColor(badgeColor)
}

internal fun LinkDetailsReplyCommentBinding.bindReplyComment(comment: LinkCommentUi.Normal, isLast: Boolean) {
    clickableContainer.setOnClick(comment.clickAction)
    if (comment.showsOption) {
        clickableContainer.setBackgroundColor(clickableContainer.context.readColorAttr(R.attr.colorControlHighlight).defaultColor)
    } else {
        clickableContainer.background = null
    }
    imgAvatar.bind(comment.author.avatar)
    txtUser.setUserNick(comment.author)
    txtTimestamp.text = comment.postedAgo
    txtApp.text = comment.app?.let { "via $it" }
    txtBody.isVisible = comment.body != null
    txtBody.text = comment.body
    imgEmbed.bind(comment.embed)
    val badgeColor = (comment.badge?.toColorInt(context = root.context) ?: root.context.readColorAttr(R.attr.colorDivider)).defaultColor
    lineCommentMiddle.setBackgroundColor(badgeColor)
    lineCommentLast.setBackgroundColor(badgeColor)
    lineAlwaysVisible.setBackgroundColor(badgeColor)
    lineHorizontal.setBackgroundColor(badgeColor)
    plusButton.bind(comment.plusCount)
    minusButton.bind(comment.minusCount)
    btnMore.setOnClick(comment.moreAction)
    lineCommentMiddle.isVisible = !isLast
    lineCommentLast.isVisible = isLast
    optionsContainer.isVisible = comment.showsOption
    btnProfile.setOnClick(comment.profileAction)
    btnShare.setOnClick(comment.shareAction)
    btnFavorite.setOnClick(comment.favoriteButton.clickAction)
    btnFavorite.setImageResource(
        if (comment.favoriteButton.isToggled) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_favorite_outlined
        },
    )
    btnFavorite.imageTintList = if (comment.favoriteButton.isToggled) {
        ColorStateList.valueOf(ContextCompat.getColor(btnFavorite.context, R.color.favorite_enabled))
    } else {
        btnFavorite.context.readColorAttr(R.attr.colorControlNormal)
    }
    btnReply.setOnClick(comment.profileAction)
}
