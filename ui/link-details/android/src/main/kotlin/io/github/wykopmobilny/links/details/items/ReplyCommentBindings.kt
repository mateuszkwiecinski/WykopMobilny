package io.github.wykopmobilny.links.details.items

import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.SelectableLinkMovement
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
    imgAvatar.bind(comment.author.avatar)
    txtUser.setUserNick(comment.author)
    txtTimestamp.text = comment.postedAgo
    txtApp.text = comment.app?.let { "via $it" }
    txtBody.isVisible = comment.body != null
    txtBody.text = comment.body
    txtBody.movementMethod = SelectableLinkMovement
    imgEmbed.bind(comment.embed)
    val badgeColor = (comment.badge?.toColorInt(context = root.context) ?: root.context.readColorAttr(R.attr.colorDivider)).defaultColor
    lineCommentMiddle.setBackgroundColor(badgeColor)
    lineCommentLast.setBackgroundColor(badgeColor)
    lineAlwaysVisible.setBackgroundColor(badgeColor)
    lineHorizontal.setBackgroundColor(badgeColor)
    plusButton.bind(comment.plusCount)
    minusButton.bind(comment.minusCount)
    moreButton.setOnClick(comment.moreAction)
    lineCommentMiddle.isVisible = !isLast
    lineCommentLast.isVisible = isLast
}
