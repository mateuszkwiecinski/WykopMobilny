package io.github.wykopmobilny.domain.navigation

interface HtmlUtils {

    suspend fun parseHtml(
        text: String,
        onLinkClicked: ((String) -> Unit)? = null,
    ): CharSequence
}
