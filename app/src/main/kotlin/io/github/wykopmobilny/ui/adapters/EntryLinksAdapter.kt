package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.models.dataclass.Entry
import io.github.wykopmobilny.models.dataclass.EntryLink
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.EntryViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.SimpleLinkViewHolder
import io.github.wykopmobilny.ui.fragments.entries.EntryActionListener
import io.github.wykopmobilny.ui.fragments.links.LinkActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class EntryLinksAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
    private val appStorage: AppStorage,
) : EndlessProgressAdapter<ViewHolder, EntryLink>() {
    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var entryActionListener: EntryActionListener
    lateinit var linkActionListener: LinkActionListener

    private val linkShowImage by lazy { settingsPreferencesApi.linkShowImage }
    private val showMinifiedImages by lazy { settingsPreferencesApi.showMinifiedImages }
    private val linkSimpleList by lazy { settingsPreferencesApi.linkSimpleList }
    private val linkImagePosition by lazy { settingsPreferencesApi.linkImagePosition }
    private val linkShowAuthor by lazy { settingsPreferencesApi.linkShowAuthor }
    private val hideBlacklistedViews by lazy { settingsPreferencesApi.hideBlacklistedViews }
    private val cutLongEntries by lazy { settingsPreferencesApi.cutLongEntries }
    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }

    override fun getViewType(position: Int): Int {
        val entryLink = dataset[position]
        return if (entryLink?.dataType == EntryLink.TYPE_ENTRY) {
            EntryViewHolder.getViewTypeForEntry(entryLink.entry!!)
        } else {
            LinkViewHolder.getViewTypeForLink(entryLink!!.link!!, linkSimpleList = linkSimpleList, linkShowImage = linkShowImage)
        }
    }

    override fun addData(items: List<EntryLink>, shouldClearAdapter: Boolean) {
        super.addData(
            items.filterNot {
                val isBlocked = it.entry?.isBlocked == true || it.link?.isBlocked == true
                hideBlacklistedViews && isBlocked
            },
            shouldClearAdapter,
        )
    }

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            LinkViewHolder.TYPE_IMAGE, LinkViewHolder.TYPE_NOIMAGE ->
                LinkViewHolder.inflateView(
                    parent = parent,
                    viewType = viewType,
                    userManagerApi = userManagerApi,
                    navigator = navigator,
                    linkActionListener = linkActionListener,
                    appStorage = appStorage,
                    linkImagePosition = linkImagePosition,
                    linkShowAuthor = linkShowAuthor,
                )
            EntryViewHolder.TYPE_BLOCKED, LinkViewHolder.TYPE_BLOCKED -> BlockedViewHolder.inflateView(parent, ::notifyItemChanged)
            else -> EntryViewHolder.inflateView(
                parent,
                viewType,
                userManagerApi,
                navigator,
                linkHandler,
                entryActionListener,
                null,
            )
        }
    }

    override fun bindHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is EntryViewHolder -> dataset[position]?.entry?.let {
                holder.bindView(
                    entry = it,
                    cutLongEntries = cutLongEntries,
                    openSpoilersDialog = openSpoilersDialog,
                    enableYoutubePlayer = enableYoutubePlayer,
                    enableEmbedPlayer = enableEmbedPlayer,
                    showAdultContent = showAdultContent,
                    hideNsfw = hideNsfw,
                )
            }
            is LinkViewHolder -> dataset[position]?.link?.let {
                holder.bindView(
                    link = it,
                    linkImagePosition = linkImagePosition,
                    linkShowAuthor = linkShowAuthor,
                )
            }
            is BlockedViewHolder -> {
                val data = dataset[position]
                data?.link?.let(holder::bindView)
                data?.entry?.let(holder::bindView)
            }
            is SimpleLinkViewHolder -> dataset[position]!!.link?.let {
                holder.bindView(it, showMinifiedImages = showMinifiedImages, linkShowImage = linkShowImage)
            }
        }
    }

    fun updateEntry(entry: Entry) {
        val position = dataset.indexOfFirst { it!!.entry?.id == entry.id }
        dataset[position]!!.entry = entry
        notifyItemChanged(position)
    }

    fun updateLink(link: Link) {
        val position = dataset.indexOfFirst { it!!.link?.id == link.id }
        dataset[position]!!.link = link
        notifyItemChanged(position)
    }
}
