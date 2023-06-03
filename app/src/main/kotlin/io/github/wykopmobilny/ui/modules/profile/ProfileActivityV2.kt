package io.github.wykopmobilny.ui.modules.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.wykopmobilny.base.ThemableActivity
import io.github.wykopmobilny.databinding.ActivityContainerBinding
import io.github.wykopmobilny.ui.profile.profileMainFragment
import io.github.wykopmobilny.utils.viewBinding

internal class ProfileActivityV2 : ThemableActivity() {

    private val binding by viewBinding(ActivityContainerBinding::inflate)

    private val userId
        get() = intent.getStringExtra(EXTRA_USER_ID).let(::checkNotNull)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, profileMainFragment(userId))
                .commit()
        }
    }

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USERNAME"

        fun createIntent(context: Context, userId: String) = Intent(context, ProfileActivityV2::class.java).apply {
            putExtra(EXTRA_USER_ID, userId)
        }
    }
}
