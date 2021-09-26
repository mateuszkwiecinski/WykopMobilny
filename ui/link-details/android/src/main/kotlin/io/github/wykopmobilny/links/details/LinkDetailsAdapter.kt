package io.github.wykopmobilny.links.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsHeaderBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsRelatedBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsReplyCommentBinding
import io.github.wykopmobilny.utils.asyncDifferConfig

internal class LinkDetailsAdapter : ListAdapter<ListItem, LinkDetailsAdapter.BindingViewHolder>(asyncDifferConfig(ListItem.Diff)) {

    override fun getItemViewType(position: Int) =
        when (getItem(position)) {
            is ListItem.Header -> R.layout.link_details_header
            is ListItem.ParentComment -> R.layout.link_details_parent_comment
            is ListItem.ReplyComment -> R.layout.link_details_reply_comment
            is ListItem.Related -> R.layout.link_details_related
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when (viewType) {
            R.layout.link_details_header -> LinkDetailsHeaderBinding.inflate(inflater, parent, false)
            R.layout.link_details_parent_comment -> LinkDetailsParentCommentBinding.inflate(inflater, parent, false)
            R.layout.link_details_reply_comment -> LinkDetailsReplyCommentBinding.inflate(inflater, parent, false)
            R.layout.link_details_related -> LinkDetailsRelatedBinding.inflate(inflater, parent, false)
            else -> error("unsupported $viewType")
        }

        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val item = getItem(position)

        when (item) {
            is ListItem.Header -> holder.binding.bindHeader(item.header)
            is ListItem.ParentComment -> holder.binding.bindParentComment(item.comment)
            is ListItem.ReplyComment -> holder.binding.bindReplyComment(item.comment)
            is ListItem.Related -> holder.binding.bindRelated(item.related)
        }
    }

    data class BindingViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)
}

private fun ViewBinding.bindRelated(related: List<RelatedLinkUi>) {
    this as LinkDetailsRelatedBinding
}

private fun ViewBinding.bindReplyComment(comment: LinkCommentUi) {
    this as LinkDetailsReplyCommentBinding
}

private fun ViewBinding.bindParentComment(comment: LinkCommentUi) {
    this as LinkDetailsParentCommentBinding
}

private fun ViewBinding.bindHeader(header: LinkDetailsHeaderUi) {
    this as LinkDetailsHeaderBinding
    when (header) {
        LinkDetailsHeaderUi.Loading -> {

        }
        is LinkDetailsHeaderUi.WithData -> {
            title.text = header.title
            description.text = header.body
            tags.text = header.tags.joinToString(separator = " ")
        }
    }
}

internal sealed class ListItem {

    data class Header(val header: LinkDetailsHeaderUi) : ListItem()

    data class Related(val related: List<RelatedLinkUi>) : ListItem()

    data class ParentComment(val comment: LinkCommentUi) : ListItem()

    data class ReplyComment(val comment: LinkCommentUi) : ListItem()

    companion object Diff : DiffUtil.ItemCallback<ListItem>() {

        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem) =
            oldItem == newItem
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun LinkDetailsUi.toAdapterList(): List<ListItem> = buildList {
    add(ListItem.Header(header))
    relatedSection?.let { add(ListItem.Related(it)) }
    commentsSection.comments.forEach { (parent, replies) ->
        add(ListItem.ParentComment(parent))
        addAll(replies.map(ListItem::ReplyComment))
    }
}
