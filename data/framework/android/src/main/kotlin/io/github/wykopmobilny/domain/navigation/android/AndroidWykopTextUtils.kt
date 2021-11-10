package io.github.wykopmobilny.domain.navigation.android

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import io.github.wykopmobilny.domain.navigation.WykopTextUtils
import io.github.wykopmobilny.domain.navigation.WykopTextUtils.RecognizedLink
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.XMLReader
import javax.inject.Inject

internal class AndroidWykopTextUtils @Inject constructor() : WykopTextUtils {

    override suspend fun parseHtml(
        text: String,
        onLinkClicked: ((RecognizedLink) -> Unit)?,
    ): CharSequence = withContext(AppDispatchers.Default) {
        val parsed = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT, null, CodeTagHandler())

        if (onLinkClicked == null) {
            parsed
        } else {
            parsed.toSpannable().apply {
                getSpans<URLSpan>().forEach { span ->
                    setSpan(
                        clickableSpan {
                            val url = span.url
                            val link = when {
                                url.startsWith("@") -> RecognizedLink.Profile(url.substringAfter("@"))
                                url.startsWith("#") -> RecognizedLink.Tag(url.substringAfter("#"))
                                url.startsWith("spoiler:") -> RecognizedLink.Spoiler(id = url.substringAfter("spoiler:"))
                                else -> RecognizedLink.Other(url)
                            }
                            onLinkClicked(link)
                        },
                        getSpanStart(span),
                        getSpanEnd(span),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
                    )
                    removeSpan(span)
                }
            }
        }
    }
}

inline fun clickableSpan(
    crossinline onClick: () -> Unit,
) = object : ClickableSpan() {

    override fun onClick(widget: View) = onClick()
}

private class CodeTagHandler : Html.TagHandler {

    override fun handleTag(opening: Boolean, tag: String, output: Editable, reader: XMLReader) {
        if (tag.equals("code", true)) {
            val len = output.length
            if (opening) {
                output.setSpan(TypefaceSpan("monospace"), len, len, Spannable.SPAN_MARK_MARK)
            } else {
                val obj = findClosing<CharacterStyle>(output)
                obj?.let {
                    val where = output.getSpanStart(obj)

                    if (obj is TypefaceSpan) {
                        output.setSpan(TypefaceSpan("monospace"), where, len, 0)
                    }
                }
            }
        }
    }

    private inline fun <reified T : Any> findClosing(text: Editable): Any? {
        val spans = text.getSpans<T>()

        return if (spans.isEmpty()) {
            null
        } else {
            (spans.size downTo 1)
                .firstOrNull { text.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                ?.let { spans[it - 1] }
        }
    }
}
