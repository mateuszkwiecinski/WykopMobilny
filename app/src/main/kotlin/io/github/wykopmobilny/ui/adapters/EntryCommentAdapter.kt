package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
import io.github.wykopmobilny.models.dataclass.EntryComment
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.EntryCommentViewHolder
import io.github.wykopmobilny.ui.fragments.entrycomments.EntryCommentActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class EntryCommentAdapter @Inject constructor(
    val userManagerApi: UserManagerApi,
    val settingsPreferencesApi: SettingsPreferencesApi,
    val navigator: NewNavigator,
    val linkHandler: WykopLinkHandler,
) : EndlessProgressAdapter<RecyclerView.ViewHolder, EntryComment>() {
    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var entryCommentActionListener: EntryCommentActionListener

    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }

    override fun getViewType(position: Int) = EntryCommentViewHolder.getViewTypeForEntryComment(dataset[position]!!)

    override fun addData(items: List<EntryComment>, shouldClearAdapter: Boolean) {
        super.addData(items.filterNot { settingsPreferencesApi.hideBlacklistedViews && it.isBlocked }, shouldClearAdapter)
    }

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EntryCommentViewHolder.TYPE_EMBED, EntryCommentViewHolder.TYPE_NORMAL ->
                EntryCommentViewHolder.inflateView(
                    parent,
                    viewType,
                    userManagerApi,
                    navigator,
                    linkHandler,
                    entryCommentActionListener,
                    null,
                    true,
                )
            else -> BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }
        }
    }

    override fun bindHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EntryCommentViewHolder -> holder.bindView(
                dataset[position]!!,
                null,
                openSpoilersDialog = openSpoilersDialog,
                enableYoutubePlayer = enableYoutubePlayer,
                enableEmbedPlayer = enableEmbedPlayer,
                showAdultContent = showAdultContent,
                hideNsfw = hideNsfw,
            )
            is BlockedViewHolder -> holder.bindView(dataset[position]!!)
        }
    }

    fun updateComment(comment: EntryComment) {
        val position = dataset.indexOf(comment).takeIf { it >= 0 } ?: return
        dataset[position] = comment
        notifyItemChanged(position)
    }
}
