package io.github.wykopmobilny.domain.navigation

interface HtmlUtils {

    fun parseHtml(
        text: String,
        onLinkClicked: ((String) -> Unit)? = null,
    ): CharSequence
}
