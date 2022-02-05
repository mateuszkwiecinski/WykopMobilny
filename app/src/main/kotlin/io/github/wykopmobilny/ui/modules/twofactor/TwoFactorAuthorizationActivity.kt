package io.github.wykopmobilny.ui.modules.twofactor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.wykopmobilny.base.ThemableActivity
import io.github.wykopmobilny.databinding.ActivityContainerBinding
import io.github.wykopmobilny.ui.twofactor.android.twoFactorMainFragment
import io.github.wykopmobilny.utils.viewBinding

internal class TwoFactorAuthorizationActivity : ThemableActivity() {

    private val binding by viewBinding(ActivityContainerBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, twoFactorMainFragment())
                .commit()
        }
    }
    companion object {

        fun createIntent(context: Context) =
            Intent(context, TwoFactorAuthorizationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
