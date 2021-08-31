package io.github.wykopmobilny.utils.textview

import android.text.Spannable
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import java.net.URLDecoder
import java.util.regex.Pattern

fun String.toSpannable(): Spannable =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT, null, CodeTagHandler()).toSpannable()

fun String.removeHtml() =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

fun String.removeSpoilerHtml(): String {
    val regexBegin = "<a href=\"spoiler:"
    val matcher = Pattern.compile("($regexBegin).*?\">\\[pokaż spoiler]</a>")
        .matcher(this)
    val matches = ArrayList<String>()
    var fullstring = this
    while (matcher.find()) {
        matches.add(matcher.group())
    }

    matches.forEach {
        val text = "! " + URLDecoder.decode(
            it.replace(regexBegin, "").replace("\">[pokaż spoiler]</a>", ""),
            "UTF-8",
        )
        fullstring = fullstring.replaceFirst(it, text)
    }

    return fullstring
}

fun String.stripWykopFormatting(): String {
    return if (contains("<a href=\"spoiler:")) {
        removeSpoilerHtml().removeHtml()
    } else removeHtml()
}
