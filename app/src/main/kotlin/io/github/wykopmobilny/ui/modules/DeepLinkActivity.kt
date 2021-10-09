package io.github.wykopmobilny.ui.modules

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler

class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.dataString ?: return finish()
        val activityToOpen = WykopLinkHandler.getLinkIntent(url, this)
            ?: MainNavigationActivity.getIntent(this)
                .also { Napier.e("Invalid deeplink for url=$url") }
        activityToOpen.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(activityToOpen)
        finish()
    }
}
