package io.github.wykopmobilny.ui.modules.links.linkdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.wykopmobilny.base.ThemableActivity
import io.github.wykopmobilny.databinding.ActivityContainerBinding
import io.github.wykopmobilny.links.details.linkDetailsFragment
import io.github.wykopmobilny.utils.viewBinding

internal class LinkDetailsActivityV2 : ThemableActivity() {

    private val binding by viewBinding(ActivityContainerBinding::inflate)

    private val linkId
        get() = intent.getStringExtra(EXTRA_LINK_ID).let(::checkNotNull)
    private val commentId
        get() = intent.getStringExtra(EXTRA_COMMENT_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, linkDetailsFragment(linkId = linkId, commentId = commentId))
                .commit()
        }
    }

    companion object {
        const val EXTRA_LINK_ID = "EXTRA_LINKID"
        const val EXTRA_COMMENT_ID = "EXTRA_COMMENT_ID"

        fun createIntent(context: Context, linkId: Long, commentId: Long? = null) =
            Intent(context, LinkDetailsActivityV2::class.java).apply {
                putExtra(EXTRA_LINK_ID, linkId)
                putExtra(EXTRA_COMMENT_ID, commentId)
            }
    }
}
