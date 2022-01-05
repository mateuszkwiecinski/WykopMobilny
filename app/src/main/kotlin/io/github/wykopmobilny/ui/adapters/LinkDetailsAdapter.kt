package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BaseLinkCommentViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkCommentViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkHeaderViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.RecyclableViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.TopLinkCommentViewHolder
import io.github.wykopmobilny.ui.fragments.link.LinkHeaderActionListener
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentActionListener
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentViewListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class LinkDetailsAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
    settingsPreferencesApi: SettingsPreferencesApi,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val showMinifiedImages by lazy { settingsPreferencesApi.showMinifiedImages }
    private val hideBlacklistedViews by lazy { settingsPreferencesApi.hideBlacklistedViews }
    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }

    var link: Link? = null
    var highlightCommentId = -1L
    lateinit var linkCommentViewListener: LinkCommentViewListener
    lateinit var linkCommentActionListener: LinkCommentActionListener
    lateinit var linkHeaderActionListener: LinkHeaderActionListener
    private val commentsList: List<LinkComment>
        get() = link?.comments
            ?.filterNot { it.isParentCollapsed || (it.isBlocked && hideBlacklistedViews) }
            .orEmpty()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try { // Suppresing, need more information to reproduce this crash.
            if (holder.itemViewType == LinkHeaderViewHolder.TYPE_HEADER) {
                link?.let {
                    (holder as LinkHeaderViewHolder).bindView(
                        link = it,
                        showMinifiedImages = false, // always use original version on details
                    )
                }
            } else if (holder is BlockedViewHolder) {
                holder.bindView(commentsList[position - 1])
            } else {
                val comment = commentsList[position - 1]
                if (holder is TopLinkCommentViewHolder) {
                    holder.bindView(
                        linkComment = comment,
                        isAuthorComment = link!!.author?.nick == comment.author.nick,
                        commentId = highlightCommentId,
                        openSpoilersDialog = openSpoilersDialog,
                        enableYoutubePlayer = enableYoutubePlayer,
                        enableEmbedPlayer = enableEmbedPlayer,
                        showAdultContent = showAdultContent,
                        hideNsfw = hideNsfw,
                    )
                    holder.itemView.tag =
                        if (comment.childCommentCount > 0 && !comment.isCollapsed) {
                            RecyclableViewHolder.SEPARATOR_SMALL
                        } else {
                            RecyclableViewHolder.SEPARATOR_NORMAL
                        }
                } else if (holder is LinkCommentViewHolder) {
                    val parent = commentsList.first { it.id == comment.parentId }
                    val index = commentsList.subList(commentsList.indexOf(parent), position - 1).size
                    holder.bindView(
                        linkComment = comment,
                        isAuthorComment = link!!.author?.nick == comment.author.nick,
                        commentId = highlightCommentId,
                        openSpoilersDialog = openSpoilersDialog,
                        enableYoutubePlayer = enableYoutubePlayer,
                        enableEmbedPlayer = enableEmbedPlayer,
                        showAdultContent = showAdultContent,
                        hideNsfw = hideNsfw,
                    )
                    holder.itemView.tag =
                        if (parent.childCommentCount == index) {
                            RecyclableViewHolder.SEPARATOR_NORMAL
                        } else {
                            RecyclableViewHolder.SEPARATOR_SMALL
                        }
                }
            }
        } catch (exception: Exception) {
            Napier.w("Couldn't bind view holder", exception)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) LinkHeaderViewHolder.TYPE_HEADER
        else BaseLinkCommentViewHolder.getViewTypeForComment(commentsList[position - 1])
    }

    override fun getItemCount() = commentsList.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LinkHeaderViewHolder.TYPE_HEADER -> LinkHeaderViewHolder.inflateView(
                parent,
                userManagerApi,
                navigator,
                linkHandler,
                linkHeaderActionListener,
            )
            TopLinkCommentViewHolder.TYPE_TOP_EMBED, TopLinkCommentViewHolder.TYPE_TOP_NORMAL -> TopLinkCommentViewHolder.inflateView(
                parent,
                viewType,
                userManagerApi,
                navigator,
                linkHandler,
                linkCommentActionListener,
                linkCommentViewListener,
            )
            LinkCommentViewHolder.TYPE_EMBED, LinkCommentViewHolder.TYPE_NORMAL -> LinkCommentViewHolder.inflateView(
                parent,
                viewType,
                userManagerApi,
                navigator,
                linkHandler,
                linkCommentActionListener,
                linkCommentViewListener,
            )
            else -> BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }
        }
    }

    fun updateLinkComment(comment: LinkComment) {
        val position = link?.comments?.indexOf(comment)?.takeIf { it >= 0 } ?: return
        link!!.comments[position] = comment
        notifyItemChanged(commentsList.indexOf(comment) + 1)
    }

    fun updateLinkHeader(link: Link) {
        this.link = link
        notifyItemChanged(0)
    }
}
