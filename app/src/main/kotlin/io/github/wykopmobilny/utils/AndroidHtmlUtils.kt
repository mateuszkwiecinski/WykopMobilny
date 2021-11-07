package io.github.wykopmobilny.utils

import androidx.core.text.HtmlCompat
import io.github.wykopmobilny.domain.utils.HtmlUtils

internal object AndroidHtmlUtils : HtmlUtils {

    override fun parseHtml(string: String): CharSequence =
        HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY)
}
