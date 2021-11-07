package io.github.wykopmobilny.utils.bindings

import androidx.annotation.DrawableRes
import io.github.wykopmobilny.ui.base.android.R
import io.github.wykopmobilny.ui.base.components.Drawable

val Drawable.drawableRes: Int
    @DrawableRes get() = when (this) {
        Drawable.Comments -> R.drawable.ic_comment
        Drawable.Sort -> R.drawable.ic_sort
        Drawable.Share -> R.drawable.ic_share
        Drawable.Upvoters -> R.drawable.ic_upvoters
        Drawable.Downvoters -> R.drawable.ic_downvoters
        Drawable.Browser -> R.drawable.ic_open_in_browser
        Drawable.PrivateMessage -> R.drawable.ic_pw
        Drawable.Copy -> R.drawable.ic_copy
        Drawable.Report -> R.drawable.ic_report
    }
