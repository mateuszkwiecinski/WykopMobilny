package io.github.wykopmobilny.domain.utils.commentparsing

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.WykopTextUtils
import io.github.wykopmobilny.domain.navigation.WykopTextUtils.RecognizedLink
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.withContext
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.net.URLDecoder

internal suspend fun String?.toCommentBody(
    textUtils: WykopTextUtils,
    expandedSpoilers: Set<ExpandedSpoiler>,
    showsSpoilersInDialog: Boolean,
    saveExpandedSpoiler: (ExpandedSpoiler) -> Unit,
    showSpoilerDialog: (suspend () -> CharSequence) -> Unit,
    onNavigation: (InteropRequest) -> Unit,
): CharSequence? = withContext(AppDispatchers.Default) {
    if (isNullOrBlank()) return@withContext null

    val allMatches = spoilerRegex.findAll(this@toCommentBody)
        .associate { it.id to it.groupValues[1].decode().convertMarkdownToHtml() }
    val withSpoilersHandled = spoilerRegex.replace(this@toCommentBody) { match ->
        if (expandedSpoilers.contains(match.id)) {
            allMatches[match.id].orEmpty()
        } else {
            """<a href="spoiler:${match.id}">[pokaż spoiler]</a>"""
        }
    }

    textUtils.parseHtml(
        text = withSpoilersHandled,
        onLinkClicked = { link ->
            when (link) {
                is RecognizedLink.Profile -> onNavigation(InteropRequest.Profile(link.profileId))
                is RecognizedLink.Tag -> onNavigation(InteropRequest.Tag(link.tagId))
                is RecognizedLink.Spoiler ->
                    if (showsSpoilersInDialog) {
                        showSpoilerDialog {
                            textUtils.parseHtml(
                                text = allMatches[link.id].orEmpty(),
                                onLinkClicked = { spoilerLink ->
                                    when (spoilerLink) {
                                        is RecognizedLink.Profile -> onNavigation(InteropRequest.Profile(spoilerLink.profileId))
                                        is RecognizedLink.Tag -> onNavigation(InteropRequest.Tag(spoilerLink.tagId))
                                        is RecognizedLink.Spoiler -> Napier.w("Spoiler dialog shouldn't have spoilers=${spoilerLink.id}")
                                        is RecognizedLink.Other -> onNavigation(InteropRequest.WebBrowser(spoilerLink.url))
                                    }
                                },
                            )
                        }
                    } else {
                        saveExpandedSpoiler(link.id)
                    }
                is RecognizedLink.Other -> onNavigation(InteropRequest.WebBrowser(link.url))
            }
        },
    )
}

private fun String.convertMarkdownToHtml(): String {
    val withHandlesAsMarkdown = handleRegex.replace(this) { match ->
        val handleType = match.groupValues[1]
        val handle = match.groupValues[2]
        "$handleType[$handle]($handleType$handle)"
    }

    val flavour = CommonMarkFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(withHandlesAsMarkdown)

    return HtmlGenerator(withHandlesAsMarkdown, parsedTree, flavour).generateHtml()
        .removePrefix("<body><p>") // https://github.com/JetBrains/markdown/issues/72
        .removeSuffix("</p></body>")
}

internal suspend fun String.copyableText(
    textUtils: WykopTextUtils,
) = withContext(AppDispatchers.Default) {
    val withSpoilersExpanded = spoilerRegex.replace(this@copyableText) { match ->
        match.groupValues[1].decode().convertMarkdownToHtml()
    }

    textUtils.parseHtml(withSpoilersExpanded).toString()
}

private fun String.decode() =
    URLDecoder.decode(this, "UTF-8")

private val MatchResult.id
    get() = range.toString()

private val handleRegex by lazy { "([#@])(\\w+)".toRegex() }
private val spoilerRegex by lazy { "<a href=\"spoiler:(.*?)\">\\[pokaż spoiler]</a>".toRegex() }

internal typealias ExpandedSpoiler = String
