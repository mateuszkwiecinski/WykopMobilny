package io.github.wykopmobilny.domain.navigation.android

import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import io.github.wykopmobilny.domain.navigation.HtmlUtils
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidHtmlUtils @Inject constructor() : HtmlUtils {

    override suspend fun parseHtml(text: String, onLinkClicked: ((String) -> Unit)?) = withContext(AppDispatchers.Default) {
        val parsed = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)

        if (onLinkClicked == null) {
            parsed
        } else {
            parsed.toSpannable().apply {
                getSpans<URLSpan>().forEach { span ->
                    setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                onLinkClicked(span.url)
                            }
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
