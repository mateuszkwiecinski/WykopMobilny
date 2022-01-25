package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BaseLinkCommentViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.TopLinkCommentViewHolder
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class LinkCommentAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
) : EndlessProgressAdapter<ViewHolder, LinkComment>() {

    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var linkCommentActionListener: LinkCommentActionListener

    private val hideBlacklistedViews by lazy { settingsPreferencesApi.hideBlacklistedViews }
    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }
    override fun getViewType(position: Int) = BaseLinkCommentViewHolder.getViewTypeForComment(data[position], true)

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TopLinkCommentViewHolder.TYPE_TOP_EMBED, TopLinkCommentViewHolder.TYPE_TOP_NORMAL ->
                TopLinkCommentViewHolder.inflateView(
                    parent = parent,
                    viewType = viewType,
                    userManagerApi = userManagerApi,
                    navigator = navigator,
                    linkHandler = linkHandler,
                    commentActionListener = linkCommentActionListener,
                    commentViewListener = null,
                )
            else -> BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }
        }
    }

    override fun addData(items: List<LinkComment>, shouldClearAdapter: Boolean) {
        super.addData(items.filterNot { hideBlacklistedViews && it.isBlocked }, shouldClearAdapter)
    }

    override fun bindHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is TopLinkCommentViewHolder -> holder.bindView(
                linkComment = data[position],
                isAuthorComment = false,
                openSpoilersDialog = openSpoilersDialog,
                enableYoutubePlayer = enableYoutubePlayer,
                enableEmbedPlayer = enableEmbedPlayer,
                showAdultContent = showAdultContent,
                hideNsfw = hideNsfw,
            )
            is BlockedViewHolder -> holder.bindView(data[position])
        }
    }

    fun updateComment(comment: LinkComment) {
        val position = data.indexOf(comment).takeIf { it >= 0 } ?: return
        dataset[position] = comment
        notifyItemChanged(position)
    }
}
