package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.SimpleLinkViewHolder
import io.github.wykopmobilny.ui.fragments.links.LinkActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class NoProgressLinkAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    private val settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val appStorage: AppStorage,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Required field, interacts with presenter. Otherwise will throw exception
    val items = mutableListOf<Link>()
    lateinit var linksActionListener: LinkActionListener

    override fun getItemViewType(position: Int): Int {
        return if (settingsPreferencesApi.linkSimpleList) {
            SimpleLinkViewHolder.getViewTypeForLink(items[position])
        } else {
            LinkViewHolder.getViewTypeForLink(items[position], settingsPreferencesApi)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SimpleLinkViewHolder.TYPE_SIMPLE_LINK -> {
                SimpleLinkViewHolder.inflateView(
                    parent = parent,
                    userManagerApi = userManagerApi,
                    settingsPreferencesApi = settingsPreferencesApi,
                    navigator = navigator,
                    linkActionListener = linksActionListener,
                    appStorage = appStorage,
                )
            }
            SimpleLinkViewHolder.TYPE_BLOCKED, LinkViewHolder.TYPE_BLOCKED ->
                BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }
            else -> LinkViewHolder.inflateView(
                parent = parent,
                viewType = viewType,
                userManagerApi = userManagerApi,
                settingsPreferencesApi = settingsPreferencesApi,
                navigator = navigator,
                linkActionListener = linksActionListener,
                appStorage = appStorage,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bindView(items[position])
            is SimpleLinkViewHolder -> holder.bindView(items[position])
            is BlockedViewHolder -> holder.bindView(items[position])
        }
    }

    override fun getItemCount() = items.size

    fun updateLink(link: Link) {
        val position = items.indexOf(link)
        items[position] = link
        notifyItemChanged(position)
    }
}
