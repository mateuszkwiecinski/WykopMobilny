package io.github.wykopmobilny.domain.navigation.android

import androidx.core.text.HtmlCompat
import io.github.wykopmobilny.domain.navigation.HtmlUtils
import javax.inject.Inject

internal class AndroidHtmlUtils @Inject constructor() : HtmlUtils {

    override fun parseHtml(string: String): CharSequence =
        HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY)
}
