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
    settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val appStorage: AppStorage,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Required field, interacts with presenter. Otherwise will throw exception
    val items = mutableListOf<Link>()
    lateinit var linksActionListener: LinkActionListener

    private val linkShowImage by lazy { settingsPreferencesApi.linkShowImage }
    private val showMinifiedImages by lazy { settingsPreferencesApi.showMinifiedImages }
    private val linkSimpleList by lazy { settingsPreferencesApi.linkSimpleList }
    private val linkImagePosition by lazy { settingsPreferencesApi.linkImagePosition }
    private val linkShowAuthor by lazy { settingsPreferencesApi.linkShowAuthor }

    override fun getItemViewType(position: Int): Int {
        return if (linkSimpleList) {
            SimpleLinkViewHolder.getViewTypeForLink(items[position])
        } else {
            LinkViewHolder.getViewTypeForLink(items[position], linkSimpleList = linkSimpleList, linkShowImage = linkShowImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SimpleLinkViewHolder.TYPE_SIMPLE_LINK -> {
                SimpleLinkViewHolder.inflateView(
                    parent = parent,
                    userManagerApi = userManagerApi,
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
                navigator = navigator,
                linkActionListener = linksActionListener,
                appStorage = appStorage,
                linkImagePosition = linkImagePosition,
                linkShowAuthor = linkShowAuthor,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bindView(
                link = items[position],
                linkImagePosition = linkImagePosition,
                showMinifiedImages = showMinifiedImages,
                linkShowAuthor = linkShowAuthor,
            )
            is SimpleLinkViewHolder -> holder.bindView(
                link = items[position],
                showMinifiedImages = showMinifiedImages,
                linkShowImage = linkShowImage,
            )
            is BlockedViewHolder -> holder.bindView(items[position])
        }
    }

    override fun getItemCount() = items.size

    fun updateLink(link: Link) {
        val position = items.indexOf(link).takeIf { it >= 0 } ?: return
        items[position] = link
        notifyItemChanged(position)
    }
}
