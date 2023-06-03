package io.github.wykopmobilny.ui.modules.links.linkdetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.github.wykopmobilny.BuildConfig
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.WykopImageFile
import io.github.wykopmobilny.api.suggest.SuggestApi
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.data.storage.api.PreferenceEntity
import io.github.wykopmobilny.databinding.ActivityLinkDetailsBinding
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.LinkDetailsAdapter
import io.github.wykopmobilny.ui.fragments.linkcomments.LinkCommentViewListener
import io.github.wykopmobilny.ui.modules.input.BaseInputActivity
import io.github.wykopmobilny.ui.widgets.InputToolbarListener
import io.github.wykopmobilny.utils.prepare
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import io.github.wykopmobilny.ui.base.android.R as BaseR

class LinkDetailsActivity :
    BaseActivity(),
    LinkDetailsView,
    androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener,
    InputToolbarListener,
    LinkCommentViewListener {

    @Inject
    lateinit var userManagerApi: UserManagerApi

    @Inject
    lateinit var settingsApi: SettingsPreferencesApi

    @Inject
    lateinit var suggestionsApi: SuggestApi

    @Inject
    lateinit var appStorage: AppStorage

    @Inject
    lateinit var adapter: LinkDetailsAdapter

    @Inject
    lateinit var presenter: LinkDetailsPresenter

    private val binding by viewBinding(ActivityLinkDetailsBinding::inflate)

    lateinit var contentUri: Uri
    override val enableSwipeBackLayout: Boolean = true
    val linkId by lazy {
        if (intent.hasExtra(EXTRA_LINK)) {
            link.id
        } else {
            intent.getLongExtra(EXTRA_LINK_ID, -1L)
        }
    }
    private val link by lazy { intent.getParcelableExtra<Link>(EXTRA_LINK).let(::checkNotNull) }
    private var replyLinkId: Long = 0
    private val linkCommentId by lazy {
        intent.getLongExtra(EXTRA_COMMENT_ID, -1)
    }

    override fun updateLinkComment(comment: LinkComment) {
        adapter.updateLinkComment(comment)
    }

    override fun replyComment(comment: LinkComment) {
        replyLinkId = comment.id
        binding.inputToolbar.addAddressant(comment.author.nick)
    }

    override fun setCollapsed(comment: LinkComment, isCollapsed: Boolean) {
        adapter.link?.comments?.forEach {
            when (comment.id) {
                it.id -> {
                    it.isCollapsed = isCollapsed
                    it.isParentCollapsed = false
                }
                it.parentId -> it.isParentCollapsed = isCollapsed
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun getReplyCommentId(): Long {
        return if (replyLinkId != 0L && binding.inputToolbar.textBody.contains("@")) {
            replyLinkId
        } else {
            -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar.toolbar)
        presenter.subscribe(this)
        adapter.linkCommentViewListener = this
        adapter.linkHeaderActionListener = presenter
        adapter.linkCommentActionListener = presenter
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.toolbar.overflowIcon = ContextCompat.getDrawable(this, BaseR.drawable.ic_sort)

        // Prepare RecyclerView
        binding.recyclerView.apply {
            prepare()
            // Set margin, adapter
            this.adapter = this@LinkDetailsActivity.adapter
        }
        supportActionBar?.title = "Znalezisko"
        presenter.linkId = linkId
        if (intent.hasExtra(EXTRA_LINK)) {
            adapter.link = link
            adapter.notifyDataSetChanged()
        }
        adapter.highlightCommentId = linkCommentId

        // Prepare InputToolbar
        binding.inputToolbar.setup(userManagerApi, suggestionsApi)
        binding.inputToolbar.inputToolbarListener = this

        binding.swiperefresh.setOnRefreshListener(this)

        presenter.sortBy = runBlocking {
            appStorage.preferencesQueries.getPreference("settings.links.comments_sort")
                .executeAsOneOrNull()
        } ?: "best"
        adapter.notifyDataSetChanged()
        binding.loadingView.isVisible = true
        hideInputToolbar()
        if (adapter.link != null) {
            presenter.loadComments()
            presenter.updateLink()
        } else {
            presenter.loadLinkAndComments()
        }

        setSubtitle()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.link_details_menu, menu)
        return true
    }

    private fun setSubtitle() {
        supportActionBar?.setSubtitle(
            when (presenter.sortBy) {
                "new" -> R.string.sortby_newest
                "old" -> R.string.sortby_oldest
                else -> R.string.sortby_best
            },
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> onRefresh()
            R.id.sortbyBest -> {
                presenter.sortBy = "best"
                appStorage.preferencesQueries.insertOrReplace(PreferenceEntity(key = "settings.links.comments_sort", value_ = "best"))
                setSubtitle()
                presenter.loadComments()
                binding.swiperefresh.isRefreshing = true
            }
            R.id.sortbyNewest -> {
                presenter.sortBy = "new"
                appStorage.preferencesQueries.insertOrReplace(PreferenceEntity(key = "settings.links.comments_sort", value_ = "new"))
                setSubtitle()
                presenter.loadComments()
                binding.swiperefresh.isRefreshing = true
            }
            R.id.sortbyOldest -> {
                presenter.sortBy = "old"
                appStorage.preferencesQueries.insertOrReplace(PreferenceEntity(key = "settings.links.comments_sort", value_ = "old"))
                setSubtitle()
                presenter.loadComments()
                binding.swiperefresh.isRefreshing = true
            }

            android.R.id.home -> finish()
        }
        return true
    }

    override fun onRefresh() {
        presenter.loadComments()
        presenter.updateLink()
        adapter.notifyDataSetChanged()
    }

    override fun showLinkComments(comments: List<LinkComment>) {
        adapter.link?.comments = comments.toMutableList()
        // Auto-Collapse comments depending on settings
        if (settingsApi.hideLinkCommentsByDefault) {
            adapter.link?.comments?.forEach {
                if (it.parentId == it.id) {
                    it.isCollapsed = true
                } else {
                    it.isParentCollapsed = true
                }
            }
        }
        binding.loadingView.isVisible = false
        binding.swiperefresh.isRefreshing = false
        adapter.notifyDataSetChanged()
        binding.inputToolbar.show()
        if (linkCommentId != -1L && adapter.link != null) {
            if (settingsApi.hideLinkCommentsByDefault) {
                expandAndScrollToComment(linkCommentId)
            } else {
                scrollToComment(linkCommentId)
            }
        }
    }

    private fun expandAndScrollToComment(linkCommentId: Long) {
        adapter.link?.comments?.let { allComments ->
            val parentId = allComments.find { it.id == linkCommentId }?.parentId
            allComments.forEach {
                if (it.parentId == parentId) {
                    it.isCollapsed = false
                    it.isParentCollapsed = false
                }
            }
        }
        adapter.notifyDataSetChanged()

        val comments = adapter.link!!.comments
        var index = 0
        for (i in 0 until comments.size) {
            if (!comments[i].isParentCollapsed) index++
            if (comments[i].id == linkCommentId) break
        }

        binding.recyclerView.scrollToPosition(index + 1)
    }

    override fun scrollToComment(id: Long) {
        val index = adapter.link?.comments?.indexOfFirst { it.id == id }?.takeIf { it >= 0 } ?: return
        binding.recyclerView.scrollToPosition(index + 1)
    }

    override fun updateLink(link: Link) {
        link.comments = adapter.link?.comments ?: mutableListOf()
        adapter.updateLinkHeader(link)
        binding.inputToolbar.show()
    }

    override fun openGalleryImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.insert_photo_galery),
            ),
            BaseInputActivity.USER_ACTION_INSERT_PHOTO,
        )
    }

    override fun sendPhoto(photo: String?, body: String, containsAdultContent: Boolean) {
        presenter.sendReply(body, photo, containsAdultContent)
    }

    override fun sendPhoto(photo: WykopImageFile, body: String, containsAdultContent: Boolean) {
        presenter.sendReply(body, photo, containsAdultContent)
    }

    override fun hideInputToolbar() {
        binding.inputToolbar.hide()
    }

    override fun hideInputbarProgress() {
        binding.inputToolbar.showProgress(false)
    }

    override fun resetInputbarState() {
        hideInputbarProgress()
        binding.inputToolbar.resetState()
        replyLinkId = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BaseInputActivity.USER_ACTION_INSERT_PHOTO -> {
                    binding.inputToolbar.setPhoto(data?.data)
                }

                BaseInputActivity.REQUEST_CODE -> {
                    onRefresh()
                }

                BaseInputActivity.USER_ACTION_INSERT_PHOTO_CAMERA -> {
                    binding.inputToolbar.setPhoto(contentUri)
                }

                BaseInputActivity.EDIT_LINK_COMMENT -> {
                    val commentId = data?.getLongExtra("commentId", -1)
                    onRefresh()
                    scrollToComment(commentId ?: -1L)
                }
            }
        }
    }

    override fun openCamera(uri: Uri) {
        contentUri = uri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, BaseInputActivity.USER_ACTION_INSERT_PHOTO_CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    companion object {
        const val EXTRA_LINK = "LINK_PARCEL"
        const val EXTRA_LINK_ID = "EXTRA_LINKID"
        const val EXTRA_COMMENT_ID = "EXTRA_COMMENT_ID"

        fun createIntent(context: Context, link: Link) = if (BuildConfig.DEBUG) {
            LinkDetailsActivityV2.createIntent(context, link.id)
            Intent(context, LinkDetailsActivity::class.java).apply {
                putExtra(EXTRA_LINK, link)
            }
        } else {
            Intent(context, LinkDetailsActivity::class.java).apply {
                putExtra(EXTRA_LINK, link)
            }
        }

        fun createIntent(context: Context, linkId: Long, commentId: Long? = null) = if (BuildConfig.DEBUG) {
            LinkDetailsActivityV2.createIntent(context, linkId = linkId, commentId = commentId)
            Intent(context, LinkDetailsActivity::class.java).apply {
                putExtra(EXTRA_LINK_ID, linkId)
                putExtra(EXTRA_COMMENT_ID, commentId)
            }
        } else {
            Intent(context, LinkDetailsActivity::class.java).apply {
                putExtra(EXTRA_LINK_ID, linkId)
                putExtra(EXTRA_COMMENT_ID, commentId)
            }
        }
    }
}
