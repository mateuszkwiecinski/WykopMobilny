package io.github.wykopmobilny.links.details.items

import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.SelectableLinkMovement
import com.github.wykopmobilny.ui.components.bind
import com.github.wykopmobilny.ui.components.setUserNick
import com.github.wykopmobilny.ui.components.utils.bind
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentHiddenBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

internal fun LinkDetailsReplyCommentHiddenBinding.bindHiddenReply(comment: LinkCommentUi.Hidden) {
    root.setOnClick(comment.onClicked)
    txtUser.setUserNick(comment.author)
    imgBadge.setBackgroundColor(comment.badge.toColorInt(context = root.context).defaultColor)
}

internal fun LinkDetailsReplyCommentBinding.bindReplyComment(comment: LinkCommentUi.Normal) {
    imgAvatar.bind(comment.author.avatar)
    txtUser.setUserNick(comment.author)
    txtTimestamp.text = comment.postedAgo
    txtApp.text = comment.app?.let { "via $it" }
    txtBody.isVisible = comment.body != null
    txtBody.text = comment.body
    txtBody.movementMethod = SelectableLinkMovement
    imgEmbed.bind(comment.embed)
    imgBadge.setBackgroundColor(comment.badge.toColorInt(context = root.context).defaultColor)
    plusButton.bind(comment.plusCount)
    minusButton.bind(comment.minusCount)
    moreButton.setOnClick(comment.moreAction)
}
