package io.github.wykopmobilny.links.details.items

import android.content.res.ColorStateList
import android.view.View
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.StandaloneTagView
import com.github.wykopmobilny.ui.components.bind
import com.github.wykopmobilny.ui.components.setUserNick
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.links.details.LinkDetailsHeaderUi
import io.github.wykopmobilny.ui.components.widgets.TagUi
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsHeaderBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

internal fun LinkDetailsHeaderBinding.bindHeader(header: LinkDetailsHeaderUi) =
    when (header) {
        LinkDetailsHeaderUi.Loading -> {
            root.isInvisible = true
        }
        is LinkDetailsHeaderUi.WithData -> {
            root.isVisible = true
            txtTitle.text = header.title
            txtTitle.setOnClick(header.viewLinkAction)
            txtDescription.text = header.body
            txtDescription.setOnClick(header.viewLinkAction)
            txtUser.setUserNick(header.author)
            txtTimestamp.text = header.postedAgo
            tagsContainer.updateTags(header.tags)
            hotBadgeStrip.isVisible = header.badge != null
            hotBadgeStrip.setBackgroundColor(header.badge.toColorInt(hotBadgeStrip.context).defaultColor)
            txtPercentage.text = header.upvotePercentage

            favoriteButton.isVisible = header.favoriteButton.isVisible
            favoriteButton.setOnClick(header.favoriteButton.clickAction)
            favoriteButton.setImageResource(
                if (header.favoriteButton.isToggled) {
                    R.drawable.ic_favorite
                } else {
                    R.drawable.ic_favorite_outlined
                },
            )
            favoriteButton.imageTintList = if (header.favoriteButton.isToggled) {
                ColorStateList.valueOf(ContextCompat.getColor(favoriteButton.context, R.color.favorite_enabled))
            } else {
                favoriteButton.context.readColorAttr(R.attr.colorControlNormal)
            }
            commentButton.bind(header.commentsCount)
            voteButton.bind(header.voteCount)
            moreButton.setOnClick(header.moreAction)
            commentSortButton.bind(header.commentsSort)
            addCommentButton.setOnClick(header.addCommentAction)
        }
    }

private fun ConstraintLayout.updateTags(tags: List<TagUi>) {
    val newValue = tags.map { it.name }
    if (tag != newValue) {
        removeAllViews()
        val tagViews = tags.map { tag ->
            StandaloneTagView(context, tag).apply {
                id = View.generateViewId()
            }
        }
        val flow = Flow(context)
        flow.referencedIds = tagViews.map { it.id }.toIntArray()
        flow.setWrapMode(Flow.WRAP_CHAIN)
        flow.setHorizontalStyle(Flow.CHAIN_PACKED)
        flow.setHorizontalBias(0f)
        flow.setVerticalAlign(Flow.VERTICAL_ALIGN_CENTER)
        addView(flow)
        tagViews.forEach(this::addView)
        tag = newValue
    }
}
