package io.github.wykopmobilny.domain.navigation

interface WykopTextUtils {

    suspend fun parseHtml(text: String, onLinkClicked: ((RecognizedLink) -> Unit)? = null): CharSequence

    sealed class RecognizedLink {

        data class Profile(val profileId: String) : RecognizedLink()

        data class Tag(val tagId: String) : RecognizedLink()

        data class Spoiler(val id: String) : RecognizedLink()

        data class Other(val url: String) : RecognizedLink()
    }
}
