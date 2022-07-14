package io.github.wykopmobilny.ui.adapters.viewholders

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.databinding.LinkLayoutBinding
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.ui.fragments.links.LinkActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.utils.loadImage
import io.github.wykopmobilny.utils.usermanager.UserManagerApi

class LinkViewHolder(
    private val binding: LinkLayoutBinding,
    private val navigator: NewNavigator,
    private val userManagerApi: UserManagerApi,
    private val linkActionListener: LinkActionListener,
    private val appStorage: AppStorage,
) : RecyclableViewHolder(binding.root) {

    companion object {
        const val ALPHA_VISITED = 0.6f
        const val ALPHA_NEW = 1f
        const val TYPE_IMAGE = 14
        const val TYPE_NOIMAGE = 15
        const val TYPE_BLOCKED = 16

        /**
         * Inflates correct view (with embed, survey or both) depending on viewType
         */
        fun inflateView(
            parent: ViewGroup,
            viewType: Int,
            userManagerApi: UserManagerApi,
            navigator: NewNavigator,
            linkActionListener: LinkActionListener,
            appStorage: AppStorage,
            linkImagePosition: String,
            linkShowAuthor: Boolean,
        ): LinkViewHolder {
            val view = LinkViewHolder(
                LinkLayoutBinding.inflate(parent.layoutInflater, parent, false),
                navigator,
                userManagerApi,
                linkActionListener,
                appStorage,
            )
            if (viewType == TYPE_IMAGE) {
                view.inflateCorrectImageView(
                    linkImagePosition = linkImagePosition,
                    linkShowAuthor = linkShowAuthor,
                )
            }
            view.type = viewType
            return view
        }

        fun getViewTypeForLink(link: Link, linkSimpleList: Boolean, linkShowImage: Boolean): Int {
            return if (linkSimpleList) {
                SimpleLinkViewHolder.getViewTypeForLink(link)
            } else {
                if (link.isBlocked) {
                    TYPE_BLOCKED
                } else if (link.fullImage == null || !linkShowImage) {
                    TYPE_NOIMAGE
                } else {
                    TYPE_IMAGE
                }
            }
        }
    }

    private var type: Int = TYPE_IMAGE
    private lateinit var previewImageView: ImageView

    fun inflateCorrectImageView(linkImagePosition: String, linkShowAuthor: Boolean) {
        previewImageView = when (linkImagePosition) {
            "top" -> {
                val img = binding.imageTop.inflate() as ImageView
                if (linkShowAuthor) {
                    val params = img.layoutParams as ViewGroup.MarginLayoutParams
                    params.topMargin = (img.context.resources.displayMetrics.density * 8).toInt()
                }
                img
            }
            "right" -> binding.imageRight.inflate() as ImageView
            "bottom" -> binding.imageBottom.inflate() as ImageView
            else -> binding.imageLeft.inflate() as ImageView
        }
    }

    fun bindView(
        link: Link,
        linkImagePosition: String,
        linkShowAuthor: Boolean,
    ) {
        setupBody(
            link = link,
            linkImagePosition = linkImagePosition,
            linkShowAuthor = linkShowAuthor,
        )
        setupButtons(link)
    }

    private fun openLinkDetail(link: Link) {
        navigator.openLinkDetailsActivity(link)
        if (!link.gotSelected) {
            setWidgetAlpha(ALPHA_VISITED)
            link.gotSelected = true
            appStorage.linksQueries.insertOrReplace(linkId = link.id)
        }
    }

    private fun setupBody(
        link: Link,
        linkImagePosition: String,
        linkShowAuthor: Boolean,
    ) {
        if (link.gotSelected) {
            setWidgetAlpha(ALPHA_VISITED)
        } else {
            setWidgetAlpha(ALPHA_NEW)
        }

        if (linkImagePosition == "left" || linkImagePosition == "right") {
            binding.titleTextView.maxLines = 2
            binding.description.maxLines = 3
        } else {
            binding.titleTextView.maxLines = Integer.MAX_VALUE
            binding.description.maxLines = Integer.MAX_VALUE
        }

        if (type == TYPE_IMAGE) {
            link.fullImage?.let(previewImageView::loadImage)
        }
        if (linkShowAuthor && link.author != null) {
            binding.authorHeaderView.setAuthorData(link.author, link.date, link.app)
            binding.authorHeaderView.isVisible = true
            binding.dateTextView.isVisible = false
        }

        binding.titleTextView.text = link.title
        binding.description.text = link.description
    }

    private fun setupButtons(link: Link) {
        binding.diggCountTextView.voteCount = link.voteCount
        binding.diggCountTextView.setup(userManagerApi)
        binding.diggCountTextView.setVoteState(link.userVote)
        when (link.userVote) {
            "dig" -> showDigged(link)
            "bury" -> showBurried(link)
            else -> showUnvoted(link)
        }

        binding.commentsCountTextView.text = link.commentsCount.toString()
        binding.dateTextView.text = link.date
        binding.hotBadgeStrip.isVisible = link.isHot
        binding.hotIcon.isVisible = link.isHot
        binding.commentsCountTextView.setOnClickListener {
            openLinkDetail(link)
        }
        binding.shareTextView.setOnClickListener {
            navigator.shareUrl(link.url)
        }
        binding.shareTextView.setOnLongClickListener {
            navigator.shareUrl(link.title + "\n" + link.description + "\n\n" + link.sourceUrl)
            true
        }
        itemView.setOnClickListener {
            openLinkDetail(link)
        }
    }

    private fun showBurried(link: Link) {
        link.userVote = "bury"
        binding.diggCountTextView.isButtonSelected = true
        binding.diggCountTextView.setVoteState("bury")
        binding.diggCountTextView.unvoteListener = {
            linkActionListener.dig(link)
            binding.diggCountTextView.isEnabled = false
        }
        binding.diggCountTextView.isEnabled = true
    }

    private fun showDigged(link: Link) {
        link.userVote = "dig"
        binding.diggCountTextView.isButtonSelected = true
        binding.diggCountTextView.setVoteState("dig")
        binding.diggCountTextView.unvoteListener = {
            linkActionListener.removeVote(link)
            binding.diggCountTextView.isEnabled = false
        }
        binding.diggCountTextView.isEnabled = true
    }

    private fun showUnvoted(link: Link) {
        binding.diggCountTextView.isButtonSelected = false
        binding.diggCountTextView.setVoteState(null)
        binding.diggCountTextView.voteListener = {
            linkActionListener.dig(link)
            binding.diggCountTextView.isEnabled = false
        }
        link.userVote = null
        binding.diggCountTextView.isEnabled = true
    }

    private fun setWidgetAlpha(alpha: Float) {
        if (type == TYPE_IMAGE) previewImageView.alpha = alpha
        binding.titleTextView.alpha = alpha
        binding.description.alpha = alpha
    }
}
