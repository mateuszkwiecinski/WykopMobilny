package io.github.wykopmobilny.ui.modules

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
import io.github.wykopmobilny.models.dataclass.Embed
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.modules.addlink.AddlinkActivity
import io.github.wykopmobilny.ui.modules.embedview.EmbedViewActivity
import io.github.wykopmobilny.ui.modules.embedview.YoutubeActivity
import io.github.wykopmobilny.ui.modules.input.BaseInputActivity
import io.github.wykopmobilny.ui.modules.input.entry.add.AddEntryActivity
import io.github.wykopmobilny.ui.modules.input.entry.comment.EditEntryCommentActivity
import io.github.wykopmobilny.ui.modules.input.entry.edit.EditEntryActivity
import io.github.wykopmobilny.ui.modules.input.link.edit.LinkCommentEditActivity
import io.github.wykopmobilny.ui.modules.links.downvoters.DownvotersActivity
import io.github.wykopmobilny.ui.modules.links.linkdetails.LinkDetailsActivity
import io.github.wykopmobilny.ui.modules.links.related.RelatedActivity
import io.github.wykopmobilny.ui.modules.links.upvoters.UpvotersActivity
import io.github.wykopmobilny.ui.modules.loginscreen.LoginScreenActivity
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import io.github.wykopmobilny.ui.modules.mikroblog.entry.EntryActivity
import io.github.wykopmobilny.ui.modules.notificationslist.NotificationsListActivity
import io.github.wykopmobilny.ui.modules.photoview.PhotoViewActivity
import io.github.wykopmobilny.ui.modules.pm.conversation.ConversationActivity
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.wykopmobilny.ui.modules.settings.SettingsActivity
import io.github.wykopmobilny.ui.modules.tag.TagActivity
import io.github.wykopmobilny.utils.openBrowser
import javax.inject.Inject

class NewNavigator @Inject constructor(
    private val context: Activity,
    private val settingsPreferences: SettingsPreferencesApi,
) {

    companion object {
        const val STARTED_FROM_NOTIFICATIONS_CODE = 228
    }

    fun openMainActivity(targetFragment: String? = null) {
        context.startActivity(
            MainNavigationActivity.getIntent(context, targetFragment)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) },
        )
    }

    fun openEntryDetailsActivity(entryId: Long, isRevealed: Boolean) =
        context.startActivity(EntryActivity.createIntent(context, entryId, null, isRevealed))

    fun openTagActivity(tag: String) =
        context.startActivity(TagActivity.createIntent(context, tag))

    fun openConversationListActivity(user: String) =
        context.startActivity(ConversationActivity.createIntent(context, user))

    fun openPhotoViewActivity(url: String) =
        context.startActivity(PhotoViewActivity.createIntent(context, url))

    fun openSettingsActivity() =
        context.startActivity(SettingsActivity.createIntent(context))

    fun openLoginScreen() =
        context.startActivity(LoginScreenActivity.createIntent(context))

    fun openAddEntryActivity(receiver: String? = null, extraBody: String? = null) =
        context.startActivity(AddEntryActivity.createIntent(context, receiver, extraBody))

    fun openEditEntryActivity(body: String, entryId: Long, embed: Embed?) =
        context.startActivityForResult(
            EditEntryActivity.createIntent(
                context = context,
                body = body,
                entryId = entryId,
                embed = embed,
            ),
            BaseInputActivity.EDIT_ENTRY,
        )

    fun openEditLinkCommentActivity(commentId: Long, body: String, linkId: Long) =
        context.startActivityForResult(
            LinkCommentEditActivity.createIntent(context, commentId, body, linkId),
            BaseInputActivity.EDIT_LINK_COMMENT,
        )

    fun openEditEntryCommentActivity(body: String, entryId: Long, commentId: Long, embed: Embed?) =
        context.startActivityForResult(
            EditEntryCommentActivity.createIntent(context, body, entryId, commentId, embed),
            BaseInputActivity.EDIT_ENTRY_COMMENT,
        )

    fun openBrowser(
        url: String,
    ) {
        if (settingsPreferences.useBuiltInBrowser) {
            context.openBrowser(url)
        } else {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        }
    }

    fun openReportScreen(violationUrl: String) =
        context.openBrowser(violationUrl)

    fun openLinkDetailsActivity(link: Link) =
        context.startActivity(LinkDetailsActivity.createIntent(context, link))

    fun openLinkUpvotersActivity(linkId: Long) =
        context.startActivity(UpvotersActivity.createIntent(linkId, context))

    fun openLinkDetailsActivity(linkId: Long, commentId: Long = -1L) =
        context.startActivity(LinkDetailsActivity.createIntent(context, linkId, commentId))

    fun openLinkDownvotersActivity(linkId: Long) =
        context.startActivity(DownvotersActivity.createIntent(linkId, context))

    fun openLinkRelatedActivity(linkId: Long) =
        context.startActivity(RelatedActivity.createIntent(linkId, context))

    fun openProfileActivity(username: String) =
        context.startActivity(ProfileActivity.createIntent(context, username))

    fun openNotificationsListActivity(preselectIndex: Int = NotificationsListActivity.PRESELECT_NOTIFICATIONS) =
        context.startActivityForResult(NotificationsListActivity.createIntent(context, preselectIndex), STARTED_FROM_NOTIFICATIONS_CODE)

    fun openEmbedActivity(url: String) =
        context.startActivity(EmbedViewActivity.createIntent(context, url))

    fun openYoutubeActivity(url: String) =
        startAndReportOnError({ YoutubeActivity.createIntent(context, url) }, "YouTube")

    fun openAddLinkActivity() =
        context.startActivity(AddlinkActivity.createIntent(context))

    fun shareUrl(url: String) {
        ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setChooserTitle(R.string.share)
            .setText(url)
            .startChooser()
    }

    private fun startAndReportOnError(intentCreator: () -> Intent, actionName: String) {
        try {
            val intent = intentCreator()
            context.startActivity(intent)
        } catch (ex: Exception) {
            Napier.e("Failed to create and start '$actionName' activity", ex)
            val message = context.getString(R.string.error_cannot_open_activity).format(actionName)
            AlertDialog.Builder(context)
                .setTitle(R.string.error_occured)
                .setMessage(message)
                .setPositiveButton(R.string.close, null)
                .create().show()
        }
    }
}
