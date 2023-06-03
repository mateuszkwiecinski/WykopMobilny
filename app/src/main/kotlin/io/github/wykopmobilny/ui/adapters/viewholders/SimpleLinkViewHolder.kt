package io.github.wykopmobilny.ui.adapters.viewholders

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import io.github.wykopmobilny.R
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.databinding.SimpleLinkLayoutBinding
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.ui.fragments.links.LinkActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.utils.loadImage
import io.github.wykopmobilny.utils.usermanager.UserManagerApi

class SimpleLinkViewHolder(
    private val binding: SimpleLinkLayoutBinding,
    private val navigator: NewNavigator,
    private val userManagerApi: UserManagerApi,
    private val linkActionListener: LinkActionListener,
    private val appStorage: AppStorage,
) : RecyclableViewHolder(binding.root) {

    companion object {
        const val ALPHA_NEW = 1f
        const val ALPHA_VISITED = 0.6f
        const val TYPE_SIMPLE_LINK = 77
        const val TYPE_BLOCKED = 78

        /**
         * Inflates correct view (with embed, survey or both) depending on viewType
         */
        fun inflateView(
            parent: ViewGroup,
            userManagerApi: UserManagerApi,
            navigator: NewNavigator,
            linkActionListener: LinkActionListener,
            appStorage: AppStorage,
        ) = SimpleLinkViewHolder(
            binding = SimpleLinkLayoutBinding.inflate(parent.layoutInflater, parent, false),
            navigator = navigator,
            userManagerApi = userManagerApi,
            linkActionListener = linkActionListener,
            appStorage = appStorage,
        )

        fun getViewTypeForLink(link: Link): Int = if (link.isBlocked) {
            TYPE_BLOCKED
        } else {
            TYPE_SIMPLE_LINK
        }
    }

    private val digCountDrawable by lazy {
        itemView.context.obtainStyledAttributes(arrayOf(R.attr.digCountDrawable).toIntArray())
            .use { it.getDrawable(0) }
    }

    fun bindView(link: Link, showMinifiedImages: Boolean, linkShowImage: Boolean) {
        setupBody(link, showMinifiedImages = showMinifiedImages, linkShowImage = linkShowImage)
    }

    private fun setupBody(link: Link, showMinifiedImages: Boolean, linkShowImage: Boolean) {
        if (link.gotSelected) {
            setWidgetAlpha(ALPHA_VISITED)
        } else {
            setWidgetAlpha(ALPHA_NEW)
        }

        when (link.userVote) {
            "dig" -> showDigged(link)
            "bury" -> showBurried(link)
            else -> showUnvoted(link)
        }
        binding.simpleDiggCount.text = link.voteCount.toString()
        binding.simpleTitle.text = link.title
        binding.hotBadgeStripSimple.isVisible = link.isHot
        binding.simpleDiggHot.isVisible = link.isHot

        binding.simpleImage.isVisible = link.fullImage != null && linkShowImage
        if (linkShowImage) {
            if (showMinifiedImages) {
                link.previewImage
            } else {
                link.fullImage
            }
                ?.let { binding.simpleImage.loadImage(it) }
        }

        itemView.setOnClickListener {
            navigator.openLinkDetailsActivity(link)
            if (!link.gotSelected) {
                setWidgetAlpha(ALPHA_VISITED)
                link.gotSelected = true
                appStorage.linksQueries.insertOrReplace(linkId = link.id)
            }
        }
    }

    private fun showBurried(link: Link) {
        link.userVote = "bury"
        binding.simpleDigg.isEnabled = true
        binding.simpleDigg.background = ContextCompat.getDrawable(itemView.context, R.drawable.ic_frame_votes_buried)
        binding.simpleDigg.setOnClickListener {
            userManagerApi.runIfLoggedIn(itemView.context) {
                binding.simpleDigg.isEnabled = false
                linkActionListener.removeVote(link)
            }
        }
    }

    private fun showDigged(link: Link) {
        link.userVote = "dig"
        binding.simpleDigg.isEnabled = true
        binding.simpleDigg.background = ContextCompat.getDrawable(itemView.context, R.drawable.ic_frame_votes_digged)
        binding.simpleDigg.setOnClickListener {
            userManagerApi.runIfLoggedIn(itemView.context) {
                binding.simpleDigg.isEnabled = false
                linkActionListener.removeVote(link)
            }
        }
    }

    private fun showUnvoted(link: Link) {
        link.userVote = null
        binding.simpleDigg.isEnabled = true
        binding.simpleDigg.background = digCountDrawable
        binding.simpleDigg.setOnClickListener {
            userManagerApi.runIfLoggedIn(itemView.context) {
                binding.simpleDigg.isEnabled = false
                linkActionListener.dig(link)
            }
        }
    }

    private fun setWidgetAlpha(alpha: Float) {
        binding.simpleImage.alpha = alpha
        binding.simpleTitle.alpha = alpha
    }
}
