package io.github.wykopmobilny.ui.modules.photoview

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.util.MimeTypes
import io.github.wykopmobilny.base.WykopSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import java.io.File
import java.nio.file.Files

interface PhotoViewCallbacks {
    fun shareImage(url: String)
    fun getDrawable(): Drawable?
    fun saveImage(url: String)
}

class PhotoViewActions(val context: Context) : PhotoViewCallbacks {

    companion object {
        const val SAVED_FOLDER = "wykopmobilny"
        const val SHARED_FOLDER = "udostępnione"
    }

    private val photoView = context as PhotoViewActivity

    override fun shareImage(url: String) {
        if (!checkForWriteReadPermission()) {
            return
        }

        Single.create(
            SingleOnSubscribe<File> {
                val file = Glide.with(context).downloadOnly().load(url).submit().get()
                val newFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "$SAVED_FOLDER/$SHARED_FOLDER/" + url.substringAfterLast("/"),
                )
                file.copyTo(newFile, true)
                it.onSuccess(newFile)
            },
        ).subscribeOn(WykopSchedulers().backgroundThread()).observeOn(WykopSchedulers().mainThread()).subscribe { file: File ->
            addImageToGallery(file.path, context)

            val url = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".fileprovider", file)
            val share = Intent(Intent.ACTION_SEND)
            share.type = getMimeType(url.path!!)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.putExtra(Intent.EXTRA_STREAM, url)
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            photoView.startActivityForResult(Intent.createChooser(share, "Udostępnij obrazek"), PhotoViewActivity.SHARE_REQUEST_CODE)
        }
    }

    override fun getDrawable(): Drawable? {
        /*photoView.image.drawable?.apply {
            return this
        }*/
        return null
    }

    override fun saveImage(url: String) {
        if (!checkForWriteReadPermission()) {
            return
        }
        saveImageV2(url)
            .subscribeOn(WykopSchedulers().backgroundThread())
            .observeOn(WykopSchedulers().mainThread()).subscribe(
                { showToastMessage("Zapisano plik") },
                { showToastMessage("Błąd podczas zapisu pliku") },
            )
    }

    private fun saveImageV2(url: String): Completable {
        return Completable.fromAction {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val source = Glide.with(context).downloadOnly().load(url).submit().get()
                val values = ContentValues().apply {
                    put(Images.Media.DISPLAY_NAME, url.substringAfterLast('/'))
                    put(Images.Media.MIME_TYPE, MimeTypes.IMAGE_JPEG)
                    put(Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + SAVED_FOLDER)
                    put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
                }

                val uri = context.contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values).let(::checkNotNull)
                context.contentResolver.openOutputStream(uri)?.use { Files.copy(source.toPath(), it) }
            } else {
                val source = Glide.with(context).downloadOnly().load(url).submit().get()
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    SAVED_FOLDER,
                )
                val targetFile = File(directory, photoView.url.substringAfterLast('/'))
                source.copyTo(targetFile, true)
                addImageToGallery(targetFile.path, context)
            }
        }
    }

    private fun checkForWriteReadPermission(): Boolean {
        ActivityCompat.requestPermissions(
            photoView,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1,
        )
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
        return writePermission != PackageManager.PERMISSION_DENIED && readPermission != PackageManager.PERMISSION_DENIED
    }

    private fun showToastMessage(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    private fun addImageToGallery(filePath: String, context: Context) {
        val values = ContentValues()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        values.put(Images.Media.MIME_TYPE, getMimeType(filePath))
        values.put(MediaStore.MediaColumns.DATA, filePath)

        context.contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun getMimeType(uri: String) =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri))
}
