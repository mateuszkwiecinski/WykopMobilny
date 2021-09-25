package io.github.wykopmobilny.domain.linkdetails

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.linkdetails.di.LinkId
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.coloredCounter
import io.github.wykopmobilny.domain.profile.plainCounter
import io.github.wykopmobilny.domain.profile.toPrettyString
import io.github.wykopmobilny.domain.profile.toUi
import io.github.wykopmobilny.domain.settings.CommentsDefaultSort
import io.github.wykopmobilny.domain.settings.prefs.GetFilteringPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetLinksPreferences
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.links.details.CommentsSectionUi
import io.github.wykopmobilny.links.details.GetLinkDetails
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.links.details.LinkDetailsHeaderUi
import io.github.wykopmobilny.links.details.LinkDetailsUi
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.components.widgets.TagUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import javax.inject.Inject

internal class GetLinkDetailsQuery @Inject constructor(
    @LinkId private val linkId: Long,
    private val detailsStore: Store<Long, LinkInfo>,
    private val getLinksPreferences: GetLinksPreferences,
    private val getFilteringPreferences: GetFilteringPreferences,
    private val commentsStore: Store<Long, Map<LinkComment, List<LinkComment>>>,
    private val viewStateStorage: SimpleViewStateStorage,
    private val appScopes: AppScopes,
    private val clock: Clock,
) : GetLinkDetails {

    override fun invoke() =
        combine(
            detailsFlow(),
            viewStateStorage.state,
            commentsFlow(),
        ) { link, viewState, comments ->
            val header = if (link == null) {
                LinkDetailsHeaderUi.Loading
            } else {
                LinkDetailsHeaderUi.WithData(
                    title = link.title,
                    body = link.description,
                    voteCount = coloredCounter(
                        userAction = link.userAction,
                        voteCount = link.voteCount,
                        onClicked = safeCallback { TODO("Vote! $linkId") },
                    ),
                    previewImageUrl = link.previewImageUrl,
                    commentsCount = plainCounter(
                        voteCount = link.commentsCount,
                        onClicked = null,
                    ),
                    isFavorite = link.userFavorite,
                    postedAgo = link.postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
                    relatedLinksCount = plainCounter(
                        voteCount = link.relatedCount,
                        onClicked = safeCallback { TODO("Related! $linkId") },
                    ),
                    author = link.author.toUi(),
                    sourceUrl = link.sourceUrl,
                    tags = link.tags.map { tag ->
                        TagUi(
                            name = tag,
                            onClick = safeCallback { TODO("TAG $tag") },
                        )
                    },
                    onAuthorClicked = safeCallback { },
                    refreshAction = safeCallback { },
                    onClicked = safeCallback { },
                    shareAction = safeCallback { },
                    favoriteAction = safeCallback { },
                )
            }
            val commentsSection = CommentsSectionUi(
                comments = comments.orEmpty(),
                isLoading = comments == null,
            )

            LinkDetailsUi(
                header = header,
                commentsSection = commentsSection,
                errorDialog = viewState.failedAction?.let { error ->
                    ErrorDialogUi(
                        error = error.cause,
                        retryAction = error.retryAction,
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(failedAction = null) } },
                    )
                },
            )
        }

    private fun commentsFlow() =
        combine(
            commentsStore.stream(StoreRequest.cached(key = linkId, refresh = false))
                .map { it.dataOrNull() },
            getLinksPreferences()
                .map { it.commentsSort }
                .distinctUntilChanged(),
            getFilteringPreferences(),
        ) { comments, sortPreferences, filteringPreferences ->
            val comparator: Comparator<LinkComment> = when (sortPreferences) {
                CommentsDefaultSort.Best -> compareBy { it.totalCount }
                CommentsDefaultSort.New -> compareByDescending { it.postedAt }
                CommentsDefaultSort.Old -> compareBy { it.postedAt }
            }
            comments
                ?.toSortedMap(comparator)
                ?.map { (key, value) ->
                    val parent = key.toUi()
                    val replies = value
                        .sortedBy { it.postedAt }
                        .map { it.toUi() }
                    parent to replies
                }
                ?.toMap()
        }

    private fun detailsFlow() =
        detailsStore.stream(StoreRequest.cached(linkId, refresh = false))
            .map { it.dataOrNull() }
            .distinctUntilChanged()

    private fun LinkComment.toUi() =
        LinkCommentUi(
            postedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(),
            app = app,
            body = body,
            author = author.toUi(),
            plusCount = coloredCounter(
                userAction = userAction.takeIf { it == UserVote.Up },
                voteCount = plusCount,
                onClicked = safeCallback { TODO("plus") },
            ),
            minusCount = coloredCounter(
                userAction = userAction.takeIf { it == UserVote.Down },
                voteCount = minusCount,
                onClicked = safeCallback { TODO("minus") },
            ),
            shareAction = safeCallback { },
            favoriteAction = safeCallback { },
        )

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safeKeyed<LinkDetailsScope>(linkId) {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(failedAction = FailedAction(failure)) } }
        }
    }
}
