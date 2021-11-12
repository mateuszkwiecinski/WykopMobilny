package io.github.wykopmobilny.links.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.wykopmobilny.links.details.items.bindHeader
import io.github.wykopmobilny.links.details.items.bindHiddenParent
import io.github.wykopmobilny.links.details.items.bindHiddenReply
import io.github.wykopmobilny.links.details.items.bindParentComment
import io.github.wykopmobilny.links.details.items.bindRelated
import io.github.wykopmobilny.links.details.items.bindReplyComment
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsHeaderBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentHiddenBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsRelatedBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentHiddenBinding
import io.github.wykopmobilny.utils.asyncDifferConfig
import io.github.wykopmobilny.utils.fixTextIsSelectableWhenUnderRecyclerView

internal class LinkDetailsAdapter : ListAdapter<ListItem, LinkDetailsAdapter.BindingViewHolder>(asyncDifferConfig(ListItem.Diff)) {

    override fun getItemViewType(position: Int) =
        when (val item = getItem(position)) {
            is ListItem.Header -> R.layout.link_details_header
            is ListItem.ParentComment -> when (item.comment.data) {
                is LinkCommentUi.Hidden -> R.layout.link_details_parent_comment_hidden
                is LinkCommentUi.Normal -> R.layout.link_details_parent_comment
            }
            is ListItem.ReplyComment -> when (item.comment) {
                is LinkCommentUi.Hidden -> R.layout.link_details_reply_comment_hidden
                is LinkCommentUi.Normal -> R.layout.link_details_reply_comment
            }
            is ListItem.RelatedSection -> R.layout.link_details_related
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when (viewType) {
            R.layout.link_details_header -> LinkDetailsHeaderBinding.inflate(inflater, parent, false)
            R.layout.link_details_parent_comment -> LinkDetailsParentCommentBinding.inflate(inflater, parent, false)
            R.layout.link_details_reply_comment -> LinkDetailsReplyCommentBinding.inflate(inflater, parent, false)
            R.layout.link_details_related -> LinkDetailsRelatedBinding.inflate(inflater, parent, false)
            R.layout.link_details_reply_comment_hidden -> LinkDetailsReplyCommentHiddenBinding.inflate(inflater, parent, false)
            R.layout.link_details_parent_comment_hidden -> LinkDetailsParentCommentHiddenBinding.inflate(inflater, parent, false)
            else -> error("unsupported $viewType")
        }

        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Header -> (holder.binding as LinkDetailsHeaderBinding).bindHeader(item.header)
            is ListItem.ParentComment -> when (val data = item.comment.data) {
                is LinkCommentUi.Normal -> (holder.binding as LinkDetailsParentCommentBinding).bindParentComment(
                    parent = item.comment,
                    data = data,
                    hasReplies = item.hasReplies,
                )
                is LinkCommentUi.Hidden -> (holder.binding as LinkDetailsParentCommentHiddenBinding).bindHiddenParent(item.comment, data)
            }
            is ListItem.ReplyComment -> when (val comment = item.comment) {
                is LinkCommentUi.Hidden -> (holder.binding as LinkDetailsReplyCommentHiddenBinding).bindHiddenReply(comment)
                is LinkCommentUi.Normal -> (holder.binding as LinkDetailsReplyCommentBinding).bindReplyComment(comment, item.isLast)
            }
            is ListItem.RelatedSection -> (holder.binding as LinkDetailsRelatedBinding).bindRelated(item.related)
        }
    }

    data class BindingViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onViewAttachedToWindow(holder: BindingViewHolder) {
        when (val binding = holder.binding) {
            is LinkDetailsParentCommentBinding -> binding.txtBody.fixTextIsSelectableWhenUnderRecyclerView()
            is LinkDetailsReplyCommentBinding -> binding.txtBody.fixTextIsSelectableWhenUnderRecyclerView()
        }
    }
}

internal sealed class ListItem {

    data class Header(val header: LinkDetailsHeaderUi) : ListItem()

    data class RelatedSection(val related: RelatedLinksSectionUi) : ListItem()

    data class ParentComment(
        val comment: ParentCommentUi,
        val hasReplies: Boolean,
    ) : ListItem() {
        val id = comment.data.id
    }

    data class ReplyComment(
        val comment: LinkCommentUi,
        val isLast: Boolean,
    ) : ListItem() {
        val id = comment.id
    }

    companion object Diff : DiffUtil.ItemCallback<ListItem>() {

        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when (oldItem) {
                is Header -> newItem is Header
                is ParentComment -> oldItem.id == (newItem as? ParentComment)?.id
                is RelatedSection -> newItem is RelatedSection
                is ReplyComment -> oldItem.id == (newItem as? ReplyComment)?.id
            }
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem) =
            oldItem == newItem

        override fun getChangePayload(oldItem: ListItem, newItem: ListItem) = newItem
    }
}

private val LinkCommentUi.id
    get() = when (this) {
        is LinkCommentUi.Hidden -> id
        is LinkCommentUi.Normal -> id
    }


@OptIn(ExperimentalStdlibApi::class)
internal fun LinkDetailsUi.toAdapterList(): List<ListItem> = buildList {
    add(ListItem.Header(header))
    commentsSection.comments.forEach { (parent, replies) ->
        add(ListItem.ParentComment(parent, hasReplies = replies.isNotEmpty()))
        addAll(replies.map { linkCommentUi -> ListItem.ReplyComment(linkCommentUi, linkCommentUi.id == replies.last().id) })
    }
}
