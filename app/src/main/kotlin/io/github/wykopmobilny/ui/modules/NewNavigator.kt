package io.github.wykopmobilny.ui.modules

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
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

interface NewNavigatorApi {
    fun openMainActivity(targetFragment: String? = null)
    fun openEntryDetailsActivity(entryId: Int, isRevealed: Boolean)
    fun openTagActivity(tag: String)
    fun openConversationListActivity(user: String)
    fun openPhotoViewActivity(url: String)
    fun openSettingsActivity()
    fun openLoginScreen()
    fun openAddEntryActivity(receiver: String? = null, extraBody: String? = null)
    fun openEditEntryActivity(body: String, entryId: Int)
    fun openEditLinkCommentActivity(commentId: Int, body: String, linkId: Int)
    fun openEditEntryCommentActivity(body: String, entryId: Int, commentId: Int)
    fun openBrowser(settingsPreferences: SettingsPreferencesApi, url: String)
    fun openReportScreen(violationUrl: String)
    fun openLinkDetailsActivity(link: Link)
    fun openLinkDetailsActivity(linkId: Int, commentId: Int = -1)
    fun openLinkUpvotersActivity(linkId: Int)
    fun openLinkDownvotersActivity(linkId: Int)
    fun openLinkRelatedActivity(linkId: Int)
    fun openProfileActivity(username: String)
    fun openNotificationsListActivity(preselectIndex: Int = NotificationsListActivity.PRESELECT_NOTIFICATIONS)
    fun openEmbedActivity(url: String)
    fun openYoutubeActivity(url: String)
    fun openAddLinkActivity()
    fun shareUrl(url: String)
}

class NewNavigator(private val context: Activity) : NewNavigatorApi {

    companion object {
        const val STARTED_FROM_NOTIFICATIONS_CODE = 228
    }

    override fun openMainActivity(targetFragment: String?) {
        context.startActivity(
            MainNavigationActivity.getIntent(context, targetFragment)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) },
        )
    }

    override fun openEntryDetailsActivity(entryId: Int, isRevealed: Boolean) =
        context.startActivity(EntryActivity.createIntent(context, entryId, null, isRevealed))

    override fun openTagActivity(tag: String) =
        context.startActivity(TagActivity.createIntent(context, tag))

    override fun openConversationListActivity(user: String) =
        context.startActivity(ConversationActivity.createIntent(context, user))

    override fun openPhotoViewActivity(url: String) =
        context.startActivity(PhotoViewActivity.createIntent(context, url))

    override fun openSettingsActivity() =
        context.startActivity(SettingsActivity.createIntent(context))

    override fun openLoginScreen() =
        context.startActivity(LoginScreenActivity.createIntent(context))

    override fun openAddEntryActivity(receiver: String?, extraBody: String?) =
        context.startActivity(AddEntryActivity.createIntent(context, receiver, extraBody))

    override fun openEditEntryActivity(body: String, entryId: Int) =
        context.startActivityForResult(EditEntryActivity.createIntent(context, body, entryId), BaseInputActivity.EDIT_ENTRY)

    override fun openEditLinkCommentActivity(commentId: Int, body: String, linkId: Int) =
        context.startActivityForResult(
            LinkCommentEditActivity.createIntent(context, commentId, body, linkId),
            BaseInputActivity.EDIT_LINK_COMMENT,
        )

    override fun openEditEntryCommentActivity(body: String, entryId: Int, commentId: Int) =
        context.startActivityForResult(
            EditEntryCommentActivity.createIntent(context, body, entryId, commentId),
            BaseInputActivity.EDIT_ENTRY_COMMENT,
        )

    override fun openBrowser(
        settingsPreferences: SettingsPreferencesApi,
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

    override fun openReportScreen(violationUrl: String) =
        context.openBrowser(violationUrl)

    override fun openLinkDetailsActivity(link: Link) =
        context.startActivity(LinkDetailsActivity.createIntent(context, link))

    override fun openLinkUpvotersActivity(linkId: Int) =
        context.startActivity(UpvotersActivity.createIntent(linkId, context))

    override fun openLinkDetailsActivity(linkId: Int, commentId: Int) =
        context.startActivity(LinkDetailsActivity.createIntent(context, linkId, commentId))

    override fun openLinkDownvotersActivity(linkId: Int) =
        context.startActivity(DownvotersActivity.createIntent(linkId, context))

    override fun openLinkRelatedActivity(linkId: Int) =
        context.startActivity(RelatedActivity.createIntent(linkId, context))

    override fun openProfileActivity(username: String) =
        context.startActivity(ProfileActivity.createIntent(context, username))

    override fun openNotificationsListActivity(preselectIndex: Int) =
        context.startActivityForResult(NotificationsListActivity.createIntent(context, preselectIndex), STARTED_FROM_NOTIFICATIONS_CODE)

    override fun openEmbedActivity(url: String) =
        context.startActivity(EmbedViewActivity.createIntent(context, url))

    override fun openYoutubeActivity(url: String) =
        startAndReportOnError({ YoutubeActivity.createIntent(context, url) }, "YouTube")

    override fun openAddLinkActivity() =
        context.startActivity(AddlinkActivity.createIntent(context))

    override fun shareUrl(url: String) {
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
