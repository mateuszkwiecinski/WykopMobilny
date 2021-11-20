package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
import io.github.wykopmobilny.models.dataclass.Entry
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.EntryListener
import io.github.wykopmobilny.ui.adapters.viewholders.EntryViewHolder
import io.github.wykopmobilny.ui.fragments.entries.EntryActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class EntriesAdapter @Inject constructor(
    val userManagerApi: UserManagerApi,
    settingsPreferencesApi: SettingsPreferencesApi,
    val navigator: NewNavigator,
    val linkHandler: WykopLinkHandler,
) : EndlessProgressAdapter<ViewHolder, Entry>() {

    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var entryActionListener: EntryActionListener

    var replyListener: EntryListener? = null

    private val hideBlacklistedViews by lazy { settingsPreferencesApi.hideBlacklistedViews }
    private val cutLongEntries by lazy { settingsPreferencesApi.cutLongEntries }
    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }

    override fun getViewType(position: Int): Int {
        val entry = dataset[position]!!
        return EntryViewHolder.getViewTypeForEntry(entry)
    }

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == EntryViewHolder.TYPE_BLOCKED) {
            BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }
        } else {
            EntryViewHolder.inflateView(
                parent = parent,
                viewType = viewType,
                userManagerApi = userManagerApi,
                navigator = navigator,
                linkHandler = linkHandler,
                entryActionListener = entryActionListener,
                replyListener = replyListener,
            )
        }
    }

    override fun addData(items: List<Entry>, shouldClearAdapter: Boolean) {
        super.addData(items.filterNot { hideBlacklistedViews && it.isBlocked }, shouldClearAdapter)
    }

    override fun bindHolder(holder: ViewHolder, position: Int) {
        if (holder is EntryViewHolder) {
            holder.bindView(
                entry = dataset[position]!!,
                cutLongEntries = cutLongEntries,
                openSpoilersDialog = openSpoilersDialog,
                enableYoutubePlayer = enableYoutubePlayer,
                enableEmbedPlayer = enableEmbedPlayer,
                showAdultContent = showAdultContent,
                hideNsfw = hideNsfw,
            )
        } else if (holder is BlockedViewHolder) {
            holder.bindView(dataset[position]!!)
        }
    }

    fun updateEntry(entry: Entry) {
        val position = dataset.indexOf(entry)
        dataset[position] = entry
        notifyItemChanged(position)
    }
}
