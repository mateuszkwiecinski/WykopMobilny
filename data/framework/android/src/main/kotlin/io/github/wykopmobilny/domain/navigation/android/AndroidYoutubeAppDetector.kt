package io.github.wykopmobilny.domain.navigation.android

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import io.github.wykopmobilny.domain.navigation.YoutubeApp
import io.github.wykopmobilny.domain.navigation.YoutubeAppDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidYoutubeAppDetector @Inject constructor(
    private val application: Application,
) : YoutubeAppDetector {

    override suspend fun getInstalledYoutubeApps(): Set<YoutubeApp> = withContext(Dispatchers.Default) {
        mapOf(
            "com.google.android.youtube" to YoutubeApp.Official,
            "com.vanced.android.youtube" to YoutubeApp.Vanced,
        )
            .filterKeys(application::isPackageInstalled)
            .values
            .toSet()
    }
}

private fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (exception: PackageManager.NameNotFoundException) {
        false
    }
}
