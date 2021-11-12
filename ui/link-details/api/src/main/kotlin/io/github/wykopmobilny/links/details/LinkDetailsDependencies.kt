package io.github.wykopmobilny.links.details

interface LinkDetailsDependencies {

    fun getLinkDetails(): GetLinkDetails
}

data class LinkDetailsKey(
    val linkId: Long,
    val initialCommentId: Long?,
)
