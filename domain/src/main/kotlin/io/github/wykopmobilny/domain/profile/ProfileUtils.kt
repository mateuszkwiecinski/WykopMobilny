package io.github.wykopmobilny.domain.profile

import io.github.wykopmobilny.domain.styles.AppTheme
import io.github.wykopmobilny.domain.styles.GetAppTheme
import io.github.wykopmobilny.ui.profile.ColorHex
import kotlinx.coroutines.flow.first

internal const val DEFAULT_PROFILE_BACKGROUND = "https://i.imgur.com/aSm6pSJ.jpg"

@Suppress("MagicNumber")
internal suspend fun Int.getNickColor(getAppTheme: GetAppTheme) = when (this) {
    0 -> "#339933"
    1 -> "#ff5917"
    2 -> "#BB0000"
    5 -> {
        when (getAppTheme().first()) {
            AppTheme.Light -> "#000000"
            AppTheme.Dark,
            AppTheme.DarkAmoled,
            -> "#FFFFFF"
        }
    }
    999 -> "#BF9B30"
    1001 -> "#999999"
    1002 -> "#999999"
    2001 -> "#3F6FA0"
    else -> "0000FF"
}.let(::ColorHex)

internal fun getGender(sex: String) =
    when (sex) {
        "male" -> "#46ABF2"
        "female" -> "#f246d0"
        else -> "#00000000"
    }.let(::ColorHex)
