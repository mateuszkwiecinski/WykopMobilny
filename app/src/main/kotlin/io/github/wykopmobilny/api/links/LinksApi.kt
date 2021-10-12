package io.github.wykopmobilny.api.links

import io.github.wykopmobilny.api.WykopImageFile
import io.github.wykopmobilny.api.responses.DigResponse
import io.github.wykopmobilny.api.responses.LinkVoteResponse
import io.github.wykopmobilny.api.responses.VoteResponse
import io.github.wykopmobilny.models.dataclass.Downvoter
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.models.dataclass.LinkVoteResponsePublishModel
import io.github.wykopmobilny.models.dataclass.Related
import io.github.wykopmobilny.models.dataclass.Upvoter
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface LinksApi {

    val burySubject: PublishSubject<LinkVoteResponsePublishModel>
    val digSubject: PublishSubject<LinkVoteResponsePublishModel>
    val voteRemoveSubject: PublishSubject<LinkVoteResponsePublishModel>

    fun getPromoted(page: Int): Single<List<Link>>
    fun getUpcoming(page: Int, sortBy: String): Single<List<Link>>

    fun getObserved(page: Int): Single<List<Link>>
    fun getLinkComments(linkId: Long, sortBy: String): Single<List<LinkComment>>
    fun getLink(linkId: Long): Single<Link>

    fun commentVoteUp(linkId: Long): Single<LinkVoteResponse>
    fun commentVoteDown(linkId: Long): Single<LinkVoteResponse>
    fun relatedVoteUp(linkId: Long, relatedId: Int): Single<VoteResponse>
    fun relatedVoteDown(linkId: Long, relatedId: Int): Single<VoteResponse>
    fun commentVoteCancel(linkId: Long): Single<LinkVoteResponse>
    fun commentDelete(commentId: Long): Single<LinkComment>
    fun commentAdd(
        body: String,
        plus18: Boolean,
        inputStream: WykopImageFile,
        linkId: Long,
        linkComment: Long,
    ): Single<LinkComment>

    fun relatedAdd(
        title: String,
        url: String,
        plus18: Boolean,
        linkId: Long,
    ): Single<Related>

    fun commentAdd(
        body: String,
        embed: String?,
        plus18: Boolean,
        linkId: Long,
        linkComment: Long,
    ): Single<LinkComment>

    fun commentAdd(
        body: String,
        plus18: Boolean,
        inputStream: WykopImageFile,
        linkId: Long,
    ): Single<LinkComment>

    fun commentAdd(
        body: String,
        embed: String?,
        plus18: Boolean,
        linkId: Long,
    ): Single<LinkComment>

    fun commentEdit(body: String, linkId: Long): Single<LinkComment>
    fun voteUp(linkId: Long, notifyPublisher: Boolean = true): Single<DigResponse>
    fun voteDown(linkId: Long, reason: Int, notifyPublisher: Boolean = true): Single<DigResponse>
    fun voteRemove(linkId: Long, notifyPublisher: Boolean = true): Single<DigResponse>
    fun getUpvoters(linkId: Long): Single<List<Upvoter>>
    fun getDownvoters(linkId: Long): Single<List<Downvoter>>
    fun markFavorite(linkId: Long): Single<Boolean>
    fun getRelated(linkId: Long): Single<List<Related>>
}
