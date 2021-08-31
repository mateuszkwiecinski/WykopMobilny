package io.github.wykopmobilny.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.ui.modules.photoview.PhotoViewActions
import java.io.File
import java.io.IOException

object CameraUtils {

    fun createPictureUri(context: Context): Uri? {
        val filename = "owmcamera_${System.currentTimeMillis()}.jpg"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "${PhotoViewActions.SAVED_FOLDER}/${PhotoViewActions.SHARED_FOLDER}/$filename",
        )

        try {
            file.parentFile?.mkdirs()
            file.createNewFile()
        } catch (exception: IOException) {
            Napier.w("Couldn't create file", exception)
        }

        return FileProvider.getUriForFile(context, context.applicationContext.packageName + ".fileprovider", file)
    }
}
