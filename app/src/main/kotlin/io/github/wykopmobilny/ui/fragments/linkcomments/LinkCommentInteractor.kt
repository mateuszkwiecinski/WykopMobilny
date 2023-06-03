package io.github.wykopmobilny.ui.fragments.linkcomments

import io.github.wykopmobilny.api.links.LinksApi
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.reactivex.Single
import javax.inject.Inject

class LinkCommentInteractor @Inject constructor(val linksApi: LinksApi) {

    fun commentVoteUp(comment: LinkComment): Single<LinkComment> = linksApi.commentVoteUp(comment.linkId, comment.id)
        .map {
            comment.voteCount = it.voteCount
            comment.voteCountPlus = it.voteCountPlus
            comment.voteCountMinus = it.voteCountMinus
            comment.userVote = 1
            comment
        }

    fun commentVoteDown(comment: LinkComment): Single<LinkComment> = linksApi.commentVoteDown(comment.linkId, comment.id)
        .map {
            comment.voteCount = it.voteCount
            comment.voteCountPlus = it.voteCountPlus
            comment.voteCountMinus = it.voteCountMinus
            comment.userVote = -1
            comment
        }

    fun commentVoteCancel(comment: LinkComment): Single<LinkComment> = linksApi.commentVoteCancel(comment.linkId, comment.id)
        .map {
            comment.voteCount = it.voteCount
            comment.voteCountPlus = it.voteCountPlus
            comment.voteCountMinus = it.voteCountMinus
            comment.userVote = 0
            comment
        }

    fun removeComment(link: LinkComment): Single<LinkComment> = linksApi.commentDelete(link.id)
        .map {
            link.embed = null
            link.body = "[Komentarz usunięty]"
            link
        }
}
