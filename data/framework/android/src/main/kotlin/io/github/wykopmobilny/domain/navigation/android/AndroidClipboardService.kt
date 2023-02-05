package io.github.wykopmobilny.domain.navigation.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import io.github.wykopmobilny.domain.navigation.ClipboardService
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidClipboardService @Inject constructor(
    private val context: Context,
) : ClipboardService {

    override suspend fun copy(text: String) = withContext(AppDispatchers.Default) {
        val clipboard = context.getSystemService<ClipboardManager>() ?: return@withContext
        val clip = ClipData.newPlainText(null, text)
        clipboard.setPrimaryClip(clip)
    }
}
