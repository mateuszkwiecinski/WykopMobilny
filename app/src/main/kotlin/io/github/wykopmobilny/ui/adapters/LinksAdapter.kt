package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
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

class LinksAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val appStorage: AppStorage,
) : EndlessProgressAdapter<RecyclerView.ViewHolder, Link>() {

    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var linksActionListener: LinkActionListener

    private val showMinifiedImages by lazy { settingsPreferencesApi.showMinifiedImages }
    private val linkSimpleList by lazy { settingsPreferencesApi.linkSimpleList }
    private val linkShowImage by lazy { settingsPreferencesApi.linkShowImage }
    private val linkImagePosition by lazy { settingsPreferencesApi.linkImagePosition }
    private val linkShowAuthor by lazy { settingsPreferencesApi.linkShowAuthor }
    private val hideBlacklistedViews by lazy { settingsPreferencesApi.linkShowAuthor }

    override fun getViewType(position: Int): Int {
        return if (linkSimpleList) {
            SimpleLinkViewHolder.getViewTypeForLink(dataset[position]!!)
        } else {
            LinkViewHolder.getViewTypeForLink(dataset[position]!!, linkSimpleList = linkSimpleList, linkShowImage = linkShowImage)
        }
    }

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SimpleLinkViewHolder.TYPE_SIMPLE_LINK ->
                SimpleLinkViewHolder.inflateView(
                    parent = parent,
                    userManagerApi = userManagerApi,
                    navigator = navigator,
                    linkActionListener = linksActionListener,
                    appStorage = appStorage,
                )
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

    override fun addData(items: List<Link>, shouldClearAdapter: Boolean) {
        super.addData(items.filterNot { hideBlacklistedViews && it.isBlocked }, shouldClearAdapter)
    }

    override fun bindHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bindView(
                link = dataset[position]!!,
                linkImagePosition = linkImagePosition,
                linkShowAuthor = linkShowAuthor,
            )
            is SimpleLinkViewHolder -> holder.bindView(
                dataset[position]!!,
                showMinifiedImages = showMinifiedImages,
                linkShowImage = linkShowImage,
            )
            is BlockedViewHolder -> holder.bindView(dataset[position]!!)
        }
    }

    fun updateLink(link: Link) {
        val position = dataset.indexOf(link).takeIf { it >= 0 } ?: return
        dataset[position] = link
        notifyItemChanged(position)
    }
}
