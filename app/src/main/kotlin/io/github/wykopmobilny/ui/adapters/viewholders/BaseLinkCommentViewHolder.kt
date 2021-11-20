package io.github.wykopmobilny.ui.adapters.viewholders

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.wykopmobilny.R
import io.github.wykopmobilny.databinding.LinkCommentMenuBottomsheetBinding
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.ui.dialogs.confirmationDialog
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentActionListener
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentViewListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.ui.widgets.WykopEmbedView
import io.github.wykopmobilny.ui.widgets.buttons.MinusVoteButton
import io.github.wykopmobilny.ui.widgets.buttons.PlusVoteButton
import io.github.wykopmobilny.utils.copyText
import io.github.wykopmobilny.utils.getActivityContext
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.textview.prepareBody
import io.github.wykopmobilny.utils.textview.stripWykopFormatting
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.utils.usermanager.isUserAuthorized
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import kotlin.math.absoluteValue

abstract class BaseLinkCommentViewHolder(
    view: View,
    private val userManagerApi: UserManagerApi,
    protected val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
    protected val commentViewListener: LinkCommentViewListener?,
    private val commentActionListener: LinkCommentActionListener,
) : RecyclableViewHolder(view) {

    companion object {
        fun getViewTypeForComment(comment: LinkComment, forceTop: Boolean = false): Int {
            return if (comment.parentId != comment.id && !forceTop) {
                when {
                    comment.isBlocked -> LinkCommentViewHolder.TYPE_BLOCKED
                    comment.embed == null -> LinkCommentViewHolder.TYPE_NORMAL
                    else -> LinkCommentViewHolder.TYPE_EMBED
                }
            } else {
                when {
                    comment.isBlocked -> TopLinkCommentViewHolder.TYPE_TOP_BLOCKED
                    comment.embed == null -> TopLinkCommentViewHolder.TYPE_TOP_NORMAL
                    else -> TopLinkCommentViewHolder.TYPE_TOP_EMBED
                }
            }
        }
    }

    private val userAuthorized by lazy { userManagerApi.isUserAuthorized() }
    private val userCredentials by lazy { userManagerApi.getUserCredentials() }

    private val collapseDrawable: Drawable? by lazy {
        itemView.context.obtainStyledAttributes(arrayOf(R.attr.collapseDrawable).toIntArray())
            .use { it.getDrawable(0) }
    }

    private val expandDrawable: Drawable? by lazy {
        itemView.context.obtainStyledAttributes(arrayOf(R.attr.expandDrawable).toIntArray())
            .use { it.getDrawable(0) }
    }

    var type: Int = 0
    abstract var embedView: WykopEmbedView
    abstract var commentContent: TextView
    abstract var replyButton: TextView
    abstract var collapseButton: ImageView
    abstract var authorBadgeStrip: View
    abstract var plusButton: PlusVoteButton
    abstract var minusButton: MinusVoteButton
    abstract var moreOptionsButton: TextView
    abstract var shareButton: TextView
    open var collapsedCommentsTextView: TextView? = null

    open fun bindView(
        linkComment: LinkComment,
        isAuthorComment: Boolean,
        commentId: Long = -1,
        openSpoilersDialog: Boolean,
        enableYoutubePlayer: Boolean,
        enableEmbedPlayer: Boolean,
        showAdultContent: Boolean,
        hideNsfw: Boolean,
    ) {
        setupBody(
            comment = linkComment,
            openSpoilersDialog = openSpoilersDialog,
            enableYoutubePlayer = enableYoutubePlayer,
            enableEmbedPlayer = enableEmbedPlayer,
            showAdultContent = showAdultContent,
            hideNsfw = hideNsfw,
        )
        setupButtons(linkComment)
        setStyleForComment(linkComment, isAuthorComment, commentId)
    }

    private fun setupBody(
        comment: LinkComment,
        openSpoilersDialog: Boolean,
        enableYoutubePlayer: Boolean,
        enableEmbedPlayer: Boolean,
        showAdultContent: Boolean,
        hideNsfw: Boolean,
    ) {
        replyButton.isVisible = userAuthorized && commentViewListener != null
        if (type == LinkCommentViewHolder.TYPE_EMBED || type == TopLinkCommentViewHolder.TYPE_TOP_EMBED) {
            embedView.setEmbed(
                embed = comment.embed,
                enableYoutubePlayer = enableYoutubePlayer,
                enableEmbedPlayer = enableEmbedPlayer,
                showAdultContent = showAdultContent,
                hideNsfw = hideNsfw,
                navigator = navigator,
                isNsfw = comment.isNsfw,
            )
        }

        comment.body?.let { body ->
            commentContent.prepareBody(
                html = body,
                urlClickListener = linkHandler::handleUrl,
                clickListener = { handleClick(comment) },
                openSpoilersDialog = openSpoilersDialog,
            )
        }

        itemView.setOnClickListener { handleClick(comment) }

        commentContent.isVisible = !comment.body.isNullOrEmpty()
        collapseButton.isVisible =
            !((comment.id != comment.parentId) || comment.childCommentCount == 0) && commentViewListener != null
    }

    private fun handleClick(comment: LinkComment) {
        // Register click listener for comments list
        if (commentViewListener == null) {
            navigator.openLinkDetailsActivity(comment.linkId, comment.id)
        }
    }

    private fun setStyleForComment(comment: LinkComment, isAuthorComment: Boolean, commentId: Long = -1) {
        if (userCredentials?.login == comment.author.nick) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBadgeOwn))
        } else if (isAuthorComment) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBadgeAuthors))
        } else {
            authorBadgeStrip.isVisible = false
        }

        if (commentId == comment.id) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.plusPressedColor))
        }
    }

    private fun setupButtons(comment: LinkComment) {
        plusButton.setup(userManagerApi)
        plusButton.text = comment.voteCountPlus.toString()
        minusButton.setup(userManagerApi)
        minusButton.text = comment.voteCountMinus.absoluteValue.toString()
        moreOptionsButton.setOnClickListener { openLinkCommentMenu(comment) }
        shareButton.setOnClickListener {
            navigator.shareUrl(comment.url)
        }
        plusButton.isEnabled = true
        minusButton.isEnabled = true

        if (comment.isCollapsed) {
            collapseButton.setImageDrawable(expandDrawable)
            collapseButton.setOnClickListener {
                commentViewListener?.setCollapsed(comment, false)
                collapsedCommentsTextView?.isVisible = comment.childCommentCount > 0
                collapsedCommentsTextView?.text = "${comment.childCommentCount} ukrytych komentarzy"
            }
        } else {
            collapseButton.setImageDrawable(collapseDrawable)
            collapseButton.setOnClickListener {
                collapsedCommentsTextView?.isVisible = false
                commentViewListener?.setCollapsed(comment, true)
            }
        }

        plusButton.voteListener = {
            commentActionListener.digComment(comment)
            minusButton.isEnabled = false
        }

        minusButton.voteListener = {
            commentActionListener.buryComment(comment)
            plusButton.isEnabled = false
        }

        plusButton.unvoteListener = {
            minusButton.isEnabled = false
            commentActionListener.removeVote(comment)
        }

        minusButton.unvoteListener = {
            plusButton.isEnabled = false
            commentActionListener.removeVote(comment)
        }

        when (comment.userVote) {
            1 -> {
                plusButton.isButtonSelected = true
                minusButton.isButtonSelected = false
            }

            0 -> {
                plusButton.isButtonSelected = false
                minusButton.isButtonSelected = false
            }

            -1 -> {
                plusButton.isButtonSelected = false
                minusButton.isButtonSelected = true
            }
        }

        replyButton.setOnClickListener {
            commentViewListener?.replyComment(comment)
        }
    }

    private fun openLinkCommentMenu(comment: LinkComment) {
        val activityContext = itemView.getActivityContext() ?: return
        val dialog = BottomSheetDialog(activityContext)
        val bottomSheetView = LinkCommentMenuBottomsheetBinding.inflate(activityContext.layoutInflater)
        dialog.setContentView(bottomSheetView.root)
        (bottomSheetView.root.parent as View).setBackgroundColor(Color.TRANSPARENT)

        bottomSheetView.apply {
            author.text = comment.author.nick
            val dateAsString = comment.fullDate.toLocalDateTime(TimeZone.currentSystemDefault()).run {
                LocalDateTime.of(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)
            }.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
            date.text = comment.app?.let { root.context.getString(R.string.date_with_user_app, dateAsString, comment.app) }
                ?: dateAsString

            commentMenuCopy.setOnClickListener {
                it.context.copyText(comment.body?.stripWykopFormatting() ?: "", "entry-body")

                dialog.dismiss()
            }

            commentMenuDelete.setOnClickListener {
                val context = it.getActivityContext() ?: return@setOnClickListener
                confirmationDialog(context) { commentActionListener.deleteComment(comment) }
                    .show()
                dialog.dismiss()
            }

            commentMenuReport.isVisible = userManagerApi.isUserAuthorized() && comment.violationUrl != null
            commentMenuReport.setOnClickListener {
                navigator.openReportScreen(comment.violationUrl.let(::checkNotNull))
                dialog.dismiss()
            }

            commentMenuEdit.setOnClickListener {
                navigator.openEditLinkCommentActivity(comment.linkId, comment.body.orEmpty(), comment.id)
                dialog.dismiss()
            }

            val canUserEdit = comment.author.nick == userManagerApi.getUserCredentials()?.login
            commentMenuDelete.isVisible = canUserEdit
            commentMenuEdit.isVisible = canUserEdit
        }

        val mBehavior = BottomSheetBehavior.from(bottomSheetView.root.parent as View)
        dialog.setOnShowListener { mBehavior.peekHeight = bottomSheetView.root.height }
        dialog.show()
    }
}
