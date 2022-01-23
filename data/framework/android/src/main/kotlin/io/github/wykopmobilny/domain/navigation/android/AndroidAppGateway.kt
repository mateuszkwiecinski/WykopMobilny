package io.github.wykopmobilny.domain.navigation.android

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.net.toUri
import io.github.wykopmobilny.domain.navigation.AppGateway
import io.github.wykopmobilny.domain.navigation.AuthenticatorApp
import io.github.wykopmobilny.domain.navigation.ExternalApp
import io.github.wykopmobilny.domain.navigation.YoutubeApp
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidAppGateway @Inject constructor(
    private val application: Application,
) : AppGateway {

    override fun getInstalledYoutubeApps() =
        application.observeInstalledPackages()
            .map {
                YoutubeApp.values()
                    .filter { application.isPackageInstalled(knownAppId(it)) }
                    .toSet()
            }
            .flowOn(AppDispatchers.Default)

    override fun getInstalledAuthenticatorApps() =
        application.observeInstalledPackages()
            .map {
                AuthenticatorApp.values()
                    .filter { application.isPackageInstalled(knownAppId(it)) }
                    .toSet()
            }
            .flowOn(AppDispatchers.Default)

    override suspend fun openApp(app: ExternalApp) = withContext(AppDispatchers.Main) {
        with(application) {
            val appId = knownAppId(app)
            if (isPackageInstalled(appId)) {
                startActivity(packageManager.getLaunchIntentForPackage(appId))
            } else {
                openStoreListing(appId)
            }
        }
    }
}

private fun Context.openStoreListing(appId: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appId".toUri()))
    } catch (ignored: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appId".toUri()))
    }
}

private fun knownAppId(app: ExternalApp) =
    when (app) {
        YoutubeApp.Official -> "com.google.android.youtube"
        YoutubeApp.Vanced -> "com.vanced.android.youtube"
        AuthenticatorApp.Google -> "com.google.android.apps.authenticator2"
        AuthenticatorApp.Microsoft -> "com.azure.authenticator"
        AuthenticatorApp.Authy -> "com.authy.authy"
    }

private fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (ignored: PackageManager.NameNotFoundException) {
        false
    }
}

private fun Context.observeInstalledPackages() =
    callbackFlow {
        val receiver = broadcastReceiver { trySendBlocking(Unit) }

        registerReceiver(receiver, getInstalledOrRemovedIntentFilter())

        awaitClose { unregisterReceiver(receiver) }
    }
        .onStart { emit(Unit) }

internal fun broadcastReceiver(callback: (Intent) -> Unit) =
    object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) =
            callback(intent)
    }

private fun getInstalledOrRemovedIntentFilter() =
    IntentFilter().apply {
        addAction("android.intent.action.PACKAGE_ADDED")
        addAction("android.intent.action.PACKAGE_REMOVED")
        addCategory("android.intent.category.DEFAULT")
        addDataScheme("package")
    }
