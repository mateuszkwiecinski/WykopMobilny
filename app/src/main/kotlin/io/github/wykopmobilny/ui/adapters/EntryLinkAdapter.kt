package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import io.github.wykopmobilny.base.adapter.AdvancedProgressAdapter
import io.github.wykopmobilny.models.dataclass.EntryLink
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.SimpleLinkViewHolder
import javax.inject.Inject

class EntryLinkAdapter @Inject constructor(
    settingsPreferencesApi: SettingsPreferencesApi,
) : AdvancedProgressAdapter<EntryLink>() {

    companion object {
        const val ENTRY_VIEWTYPE = 1
        const val LINK_VIEWTYPE = 2
        const val SIMPLE_LINK_VIEWTYPE = 3
    }

    private val linkSimpleList by lazy { settingsPreferencesApi.linkSimpleList }
    private val linkShowImage by lazy { settingsPreferencesApi.linkShowImage }
    private val showMinifiedImages by lazy { settingsPreferencesApi.showMinifiedImages }
    private val linkImagePosition by lazy { settingsPreferencesApi.linkImagePosition }
    private val linkShowAuthor by lazy { settingsPreferencesApi.linkShowAuthor }

    override fun getItemViewType(position: Int): Int = when {
        dataset[position] == null -> VIEWTYPE_PROGRESS
        dataset[position]!!.entry != null -> ENTRY_VIEWTYPE
        else -> if (linkSimpleList) SIMPLE_LINK_VIEWTYPE else LINK_VIEWTYPE
    }

    override fun createViewHolder(viewType: Int, parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder =
        BlockedViewHolder.inflateView(parent) { notifyItemChanged(it) }

    override fun bindHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = dataset[position]!!
        if (item.entry != null) {
            (holder as BlockedViewHolder).bindView(item.entry!!)
        } else if (item.link != null) {
            if (holder is SimpleLinkViewHolder) {
                holder.bindView(item.link!!, showMinifiedImages, linkShowImage = linkShowImage)
            } else {
                (holder as? LinkViewHolder)?.bindView(
                    link = item.link!!,
                    linkImagePosition = linkImagePosition,
                    linkShowAuthor = linkShowAuthor,
                )
            }
        }
    }
}
