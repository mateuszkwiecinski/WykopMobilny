package io.github.wykopmobilny.api.profile

import io.github.wykopmobilny.api.UserTokenRefresher
import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.errorhandler.ErrorHandlerTransformer
import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.responses.BadgeResponse
import io.github.wykopmobilny.api.responses.ProfileResponse
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.models.dataclass.EntryComment
import io.github.wykopmobilny.models.dataclass.EntryLink
import io.github.wykopmobilny.models.dataclass.LinkComment
import io.github.wykopmobilny.models.dataclass.Related
import io.github.wykopmobilny.models.mapper.apiv2.EntryCommentMapper
import io.github.wykopmobilny.models.mapper.apiv2.EntryLinkMapper
import io.github.wykopmobilny.models.mapper.apiv2.LinkCommentMapper
import io.github.wykopmobilny.models.mapper.apiv2.RelatedMapper
import io.github.wykopmobilny.models.mapper.apiv2.filterEntries
import io.github.wykopmobilny.models.mapper.apiv2.filterLinks
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileRetrofitApi,
    private val userTokenRefresher: UserTokenRefresher,
    private val owmContentFilter: OWMContentFilter,
    private val appStorage: AppStorage,
) : ProfileApi {

    override fun getIndex(username: String): Single<ProfileResponse> = rxSingle { profileApi.getIndex(username) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun getAdded(username: String, page: Int) = rxSingle { profileApi.getAdded(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getActions(username: String): Single<List<EntryLink>> = rxSingle { profileApi.getActions(username) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.map { EntryLinkMapper.map(it, owmContentFilter) } }

    override fun getPublished(username: String, page: Int) = rxSingle { profileApi.getPublished(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getEntries(username: String, page: Int) = rxSingle { profileApi.getEntries(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.filterEntries(owmContentFilter = owmContentFilter) }

    override fun getEntriesComments(username: String, page: Int): Single<List<EntryComment>> =
        rxSingle { profileApi.getEntriesComments(username, page) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())
            .map { it.map { EntryCommentMapper.map(it, owmContentFilter) } }

    override fun getLinkComments(username: String, page: Int): Single<List<LinkComment>> =
        rxSingle { profileApi.getLinkComments(username, page) }
            .retryWhen(userTokenRefresher)
            .compose(ErrorHandlerTransformer())
            .map { it.map { LinkCommentMapper.map(it, owmContentFilter) } }

    override fun getBuried(username: String, page: Int) = rxSingle { profileApi.getBuried(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getDigged(username: String, page: Int) = rxSingle { profileApi.getDigged(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun getBadges(username: String, page: Int): Single<List<BadgeResponse>> = rxSingle { profileApi.getBadges(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun getRelated(username: String, page: Int): Single<List<Related>> = rxSingle { profileApi.getRelated(username, page) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .map { it.map { RelatedMapper.map(it) } }

    override fun observe(tag: String) = rxSingle { profileApi.observe(tag) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun unobserve(tag: String) = rxSingle { profileApi.unobserve(tag) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())

    override fun block(tag: String) = rxSingle { profileApi.block(tag) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .flatMap { response ->
            rxSingle {
                appStorage.blacklistQueries.insertOrReplaceProfile(tag.removePrefix("@"))
                response
            }
        }

    override fun unblock(tag: String) = rxSingle { profileApi.unblock(tag) }
        .retryWhen(userTokenRefresher)
        .compose(ErrorHandlerTransformer())
        .flatMap { response ->
            rxSingle {
                appStorage.blacklistQueries.deleteProfile(tag.removePrefix("@"))
                response
            }
        }
}
