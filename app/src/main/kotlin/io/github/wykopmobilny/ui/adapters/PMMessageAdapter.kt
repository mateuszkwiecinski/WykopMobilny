package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.databinding.PmmessageSentLayoutBinding
import io.github.wykopmobilny.models.dataclass.PMMessage
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.PMMessageViewHolder
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import javax.inject.Inject

class PMMessageAdapter @Inject constructor(
    settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
) : RecyclerView.Adapter<PMMessageViewHolder>() {

    val messages: ArrayList<PMMessage> = arrayListOf()

    private val openSpoilersDialog by lazy { settingsPreferencesApi.openSpoilersDialog }
    private val enableEmbedPlayer by lazy { settingsPreferencesApi.enableEmbedPlayer }
    private val enableYoutubePlayer by lazy { settingsPreferencesApi.enableYoutubePlayer }
    private val showAdultContent by lazy { settingsPreferencesApi.showAdultContent }
    private val hideNsfw by lazy { settingsPreferencesApi.hideNsfw }

    override fun getItemCount() = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PMMessageViewHolder(
        binding = PmmessageSentLayoutBinding.inflate(parent.layoutInflater, parent, false),
        linkHandler = linkHandler,
        navigator = navigator,
    )

    override fun onBindViewHolder(holder: PMMessageViewHolder, position: Int) = holder.bindView(
        message = messages[position],
        openSpoilersDialog = openSpoilersDialog,
        enableEmbedPlayer = enableEmbedPlayer,
        enableYoutubePlayer = enableYoutubePlayer,
        showAdultContent = showAdultContent,
        hideNsfw = hideNsfw,
    )

    override fun onViewRecycled(holder: PMMessageViewHolder) {
        holder.cleanRecycled()
        super.onViewRecycled(holder)
    }
}
