package com.github.wykopmobilny.ui.components

import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi
import io.github.wykopmobilny.utils.bindings.setOnClick

fun TextView.setUserNick(userInfoUi: UserInfoUi?) {
    isVisible = userInfoUi != null
    text = userInfoUi?.let { info ->
        buildSpannedString {
            color(info.color.toColorInt(context).defaultColor) {
                append("@")
                append(info.name)
            }
        }
    }
    setOnClick(userInfoUi?.avatar?.onClicked)
}
