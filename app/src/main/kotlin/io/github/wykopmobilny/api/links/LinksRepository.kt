package io.github.wykopmobilny.api.links

import io.github.wykopmobilny.api.UserTokenRefresher
import io.github.wykopmobilny.api.WykopImageFile
import io.github.wykopmobilny.api.endpoints.LinksRetrofitApi
import io.github.wykopmobilny.api.entries.allowImageOnly
import io.github.wykopmobilny.api.errorhandler.ErrorHandlerTransformer
import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.patrons.PatronsApi
import io.github.wykopmobilny.api.toRequestBody
import io.github.wykopmobilny.models.dataclass.LinkVoteResponsePublishModel
import io.github.wykopmobilny.models.mapper.apiv2.DownvoterMapper
import io.github.wykopmobilny.models.mapper.apiv2.LinkCommentMapper
import io.github.wykopmobilny.models.mapper.apiv2.RelatedMapper
import io.github.wykopmobilny.models.mapper.apiv2.UpvoterMapper
import io.github.wykopmobilny.models.mapper.apiv2.filterLink
import io.github.wykopmobilny.models.mapper.apiv2.filterLinks
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinksRepository @Inject constructor(
    private val linksApi: LinksRetrofitApi,
    private val userTokenRefresher: UserTokenRefresher,
    private val owmContentFilter: OWMContentFilter,
    private val patronsApi: PatronsApi,
) : LinksApi {

    override val voteRemoveSubject = PublishSubject.create<LinkVoteResponsePublishModel>()
    override val digSubject = PublishSubject.create<LinkVoteResponsePublishModel>()
    override val burySubject = PublishSubject.create<LinkVoteResponsePublishModel>()

    override fun getPromoted(page: Int) = rxSingle { linksApi.getPromoted(page) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getUpcoming(page: Int, sortBy: String) = rxSingle { linksApi.getUpcoming(page, sortBy) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getObserved(page: Int) = rxSingle { linksApi.getObserved(page) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getLinkComments(linkId: Long, sortBy: String) = rxSingle { linksApi.getLinkComments(linkId, sortBy) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .map { it.map { response -> LinkCommentMapper.map(response, owmContentFilter) } }
        .map { list ->
            list.forEach { comment ->
                if (
                    comment.id == comment.parentId
                ) {
                    comment.childCommentCount = list.filter { item -> comment.id == item.parentId }.size - 1
                }
            }
            list
        }

    override fun getLink(linkId: Long) = rxSingle { linksApi.getLink(linkId) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .map { it.filterLink(owmContentFilter = owmContentFilter) }

    override fun commentVoteUp(linkId: Long, commentId: Long) = rxSingle { linksApi.commentVoteUp(linkId = linkId, commentId = commentId) }
        .flatMap { patronsApi.ensurePatrons(it) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun commentVoteDown(linkId: Long, commentId: Long) =
        rxSingle { linksApi.commentVoteDown(linkId = linkId, commentId = commentId) }
            .flatMap { patronsApi.ensurePatrons(it) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())

    override fun relatedVoteUp(linkId: Long, relatedId: Int) = rxSingle { linksApi.relatedVoteUp(linkId, relatedId.toLong()) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun relatedVoteDown(linkId: Long, relatedId: Int) = rxSingle { linksApi.relatedVoteDown(linkId, relatedId.toLong()) }
        .flatMap { patronsApi.ensurePatrons(it) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun commentVoteCancel(linkId: Long, commentId: Long) =
        rxSingle { linksApi.commentVoteCancel(linkId = linkId, commentId = commentId) }
            .flatMap { patronsApi.ensurePatrons(it) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())

    override fun voteUp(linkId: Long, notifyPublisher: Boolean) = rxSingle { linksApi.voteUp(linkId) }
        .flatMap { patronsApi.ensurePatrons(it) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .doOnSuccess {
            if (notifyPublisher) {
                digSubject.onNext(LinkVoteResponsePublishModel(linkId, it))
            }
        }

    override fun voteDown(linkId: Long, reason: Int, notifyPublisher: Boolean) = rxSingle { linksApi.voteDown(linkId, reason) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .doOnSuccess {
            if (notifyPublisher) {
                burySubject.onNext(LinkVoteResponsePublishModel(linkId, it))
            }
        }

    override fun voteRemove(linkId: Long, notifyPublisher: Boolean) = rxSingle { linksApi.voteRemove(linkId) }
        .retryWhen(userTokenRefresher)
        .flatMap { patronsApi.ensurePatrons(it) }
        .compose(ErrorHandlerTransformer())
        .doOnSuccess {
            if (notifyPublisher) {
                voteRemoveSubject.onNext(LinkVoteResponsePublishModel(linkId, it))
            }
        }

    override fun commentAdd(body: String, embed: String?, plus18: Boolean, linkId: Long) =
        rxSingle { linksApi.addComment(body, linkId, embed, plus18) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())
            .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun relatedAdd(title: String, url: String, plus18: Boolean, linkId: Long) =
        rxSingle { linksApi.addRelated(title, linkId, url, plus18) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())
            .map { RelatedMapper.map(it) }

    override fun commentAdd(body: String, plus18: Boolean, inputStream: WykopImageFile, linkId: Long) = rxSingle {
        linksApi.addComment(
            body = body.allowImageOnly().toRequestBody(),
            plus18 = plus18.toRequestBody(),
            linkId = linkId,
            file = inputStream.getFileMultipart(),
        )
    }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun commentAdd(body: String, embed: String?, plus18: Boolean, linkId: Long, linkComment: Long) =
        rxSingle { linksApi.addComment(body, linkId, linkComment, embed, plus18) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())
            .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun commentAdd(body: String, plus18: Boolean, inputStream: WykopImageFile, linkId: Long, linkComment: Long) = rxSingle {
        linksApi.addComment(
            body = body.allowImageOnly().toRequestBody(),
            plus18 = plus18.toRequestBody(),
            linkId = linkId,
            commentId = linkComment,
            file = inputStream.getFileMultipart(),
        )
    }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun commentEdit(body: String, linkId: Long) = rxSingle { linksApi.editComment(body, linkId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun commentDelete(commentId: Long) = rxSingle { linksApi.deleteComment(commentId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { LinkCommentMapper.map(it, owmContentFilter) }

    override fun getDownvoters(linkId: Long) = rxSingle { linksApi.getDownvoters(linkId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.map { response -> DownvoterMapper.map(response) } }

    override fun getUpvoters(linkId: Long) = rxSingle { linksApi.getUpvoters(linkId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.map { response -> UpvoterMapper.map(response) } }

    override fun getRelated(linkId: Long) = rxSingle { linksApi.getRelated(linkId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.map { response -> RelatedMapper.map(response) } }

    override fun markFavorite(linkId: Long) = rxSingle { linksApi.toggleFavorite(linkId) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { }
}
