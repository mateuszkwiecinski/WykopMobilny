package io.github.wykopmobilny.ui.widgets.markdowntoolbar

import android.net.Uri

interface MarkdownToolbarListener {
    var selectionStart: Int
    var selectionEnd: Int
    var textBody: String
    fun setSelection(start: Int, end: Int)
    fun openGalleryImageChooser()
    fun openCamera(uri: Uri)
}
