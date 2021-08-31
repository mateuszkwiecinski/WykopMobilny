package io.github.wykopmobilny.utils.linkhandler.linkparser

object LinkParser {

    fun getLinkId(url: String): Long? {
        if (url.contains("/link/")) {
            return url.substringAfter("/link/").substringBefore("/").toLongOrNull()
        }
        return null
    }

    fun getLinkCommentId(url: String): Long? {
        if (url.contains("/#comment-")) {
            return url.substringAfter("/#comment-").substringBefore("/").toLongOrNull()
        }
        return null
    }
}
