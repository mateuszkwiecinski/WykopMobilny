package io.github.wykopmobilny.domain.linkdetails

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.linkdetails.di.LinkId
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.coloredCounter
import io.github.wykopmobilny.domain.profile.toPrettyString
import io.github.wykopmobilny.domain.profile.toUi
import io.github.wykopmobilny.domain.profile.wykopUrl
import io.github.wykopmobilny.domain.repositories.LinksRepository
import io.github.wykopmobilny.domain.settings.CommentsDefaultSort
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.prefs.FilteringPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetFilteringPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetImagesPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetLinksPreferences
import io.github.wykopmobilny.domain.settings.prefs.ImagePreferences
import io.github.wykopmobilny.domain.settings.update
import io.github.wykopmobilny.domain.strings.Strings
import io.github.wykopmobilny.domain.utils.HtmlUtils
import io.github.wykopmobilny.domain.utils.combine
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.links.details.CommentsSectionUi
import io.github.wykopmobilny.links.details.GetLinkDetails
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.links.details.LinkDetailsHeaderUi
import io.github.wykopmobilny.links.details.LinkDetailsMenuOption
import io.github.wykopmobilny.links.details.LinkDetailsUi
import io.github.wykopmobilny.links.details.ParentCommentUi
import io.github.wykopmobilny.links.details.RelatedLinkUi
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.OptionPickerUi
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.ColorConst
import io.github.wykopmobilny.ui.components.widgets.TagUi
import io.github.wykopmobilny.ui.components.widgets.ToggleButtonUi
import io.github.wykopmobilny.ui.components.widgets.TwoActionsCounterUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import java.net.URL
import javax.inject.Inject
import kotlin.math.roundToInt

internal class GetLinkDetailsQuery @Inject constructor(
    @LinkId private val linkId: Long,
    private val linkStore: Store<Long, LinkInfo>,
    private val getLinksPreferences: GetLinksPreferences,
    private val getFilteringPreferences: GetFilteringPreferences,
    private val getImagePreferences: GetImagesPreferences,
    private val userInfoStorage: UserInfoStorage,
    private val commentsStore: Store<Long, Map<LinkComment, List<LinkComment>>>,
    private val viewStateStorage: LinkDetailsViewStateStorage,
    private val appScopes: AppScopes,
    private val appStorage: AppStorage,
    private val clock: Clock,
    private val interopRequests: InteropRequestsProvider,
    private val blacklistStore: Store<Unit, Blacklist>,
    private val linksRepository: LinksRepository,
    private val htmlUtils: HtmlUtils,
) : GetLinkDetails {

    override fun invoke() =
        combine(
            detailsFlow(),
            viewStateStorage.state,
            commentsFlow(),
            userInfoStorage.loggedUser,
            getLinksPreferences(),
        ) { link, viewState, (comments, toggleCommentsAction), loggedUser, linkpreferences ->
            val header = if (link == null) {
                LinkDetailsHeaderUi.Loading
            } else {
                LinkDetailsHeaderUi.WithData(
                    title = htmlUtils.parseHtml(link.title).toString(),
                    body = htmlUtils.parseHtml(link.description).toString(),
                    domain = URL(link.sourceUrl).host.removePrefix("www."),
                    badge = ColorConst.LinkBadgeHot.takeIf { link.isHot },
                    voteCount = TwoActionsCounterUi(
                        count = link.voteCount,
                        color = when (link.userAction) {
                            UserVote.Up -> ColorConst.CounterUpvoted
                            UserVote.Down -> ColorConst.CounterDownvoted
                            null -> null
                        },
                        upvoteAction = safeCallback {
                            when (link.userAction) {
                                UserVote.Up -> linksRepository.removeVote(link.id)
                                UserVote.Down,
                                null,
                                -> linksRepository.voteUp(link.id)
                            }
                        },
                        downvoteAction = safeCallback {
                            when (link.userAction) {
                                UserVote.Down -> linksRepository.removeVote(link.id)
                                UserVote.Up,
                                null,
                                -> viewStateStorage.update { viewState ->
                                    viewState.copy(
                                        picker = OptionPickerUi(
                                            title = Strings.Link.BURY_REASON_TITLE,
                                            reasons = VoteDownReason.values().map { reason ->
                                                OptionPickerUi.Option(
                                                    label = reason.label,
                                                    clickAction = safeCallback { linksRepository.voteDown(link.id, reason) },
                                                )
                                            },
                                            dismissAction = safeCallback { viewStateStorage.update { it.copy(picker = null) } },
                                        ),
                                    )
                                }
                            }
                        },
                    ),
                    upvotePercentage = link.upvotePercentage,
                    previewImageUrl = link.previewImageUrl,
                    commentsCount = Button(
                        label = link.commentsCount.toString(),
                        icon = Drawable.Comments,
                        clickAction = toggleCommentsAction,
                    ),
                    postedAgo = link.postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
                    author = link.author.toUi(
                        onClicked = safeCallback { interopRequests.request(InteropRequest.Profile(link.author.profileId)) },
                    ),
                    tags = link.tags.map { tag ->
                        TagUi(
                            name = tag,
                            onClick = safeCallback { interopRequests.request(InteropRequest.Tag(tag)) },
                        )
                    },
                    viewLinkAction = safeCallback { interopRequests.request(InteropRequest.WebBrowser(link.sourceUrl)) },
                    favoriteButton = ToggleButtonUi(
                        isToggled = link.userFavorite,
                        clickAction = safeCallback { linksRepository.toggleFavorite(link.id) },
                        isVisible = loggedUser != null,
                    ),
                    moreAction = safeCallback {
                        viewStateStorage.update { viewState ->
                            viewState.copy(
                                picker = OptionPickerUi(
                                    title = Strings.Link.MORE_TITLE,
                                    reasons = listOf(
                                        OptionPickerUi.Option(
                                            label = Strings.Link.MORE_OPTION_SHARE,
                                            icon = Drawable.Share,
                                            clickAction = safeCallback { interopRequests.request(InteropRequest.Share(link.wykopUrl)) },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.moreOptionUpvotersList(link.voteCount),
                                            icon = Drawable.Upvoters,
                                            clickAction = safeCallback { interopRequests.request(InteropRequest.UpvotersList(link.id)) },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.moreOptioDownvotersList(link.buryCount),
                                            icon = Drawable.Downvoters,
                                            clickAction = safeCallback { interopRequests.request(InteropRequest.DownvotersList(link.id)) },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.MORE_OPTION_OPEN_IN_BROWSER,
                                            icon = Drawable.Browser,
                                            clickAction = safeCallback {
                                                interopRequests.request(InteropRequest.WebBrowser(url = link.wykopUrl, force = true))
                                            },
                                        ),
                                    ),
                                    dismissAction = safeCallback { viewStateStorage.update { it.copy(picker = null) } },
                                ),
                            )
                        }
                    },
                    commentsSort = Button(
                        label = linkpreferences.commentsSort.label,
                        icon = Drawable.Sort,
                        clickAction = safeCallback {
                            viewStateStorage.update { viewState ->
                                viewState.copy(
                                    picker = OptionPickerUi(
                                        title = Strings.Link.COMMENTS_SORT_TITLE,
                                        reasons = CommentsDefaultSort.values().map { option ->
                                            OptionPickerUi.Option(
                                                label = Strings.Link.commentsSortOption(option.label),
                                                clickAction = safeCallback { appStorage.update(UserSettings.commentsSort, option) },
                                            )
                                        },
                                        dismissAction = safeCallback { viewStateStorage.update { it.copy(picker = null) } },
                                    ),
                                )
                            }
                        },
                    ),
                    addCommentAction = safeCallback { TODO("Not supported") },
                )
            }
            val commentsSection = CommentsSectionUi(
                comments = comments,
                isLoading = comments.isEmpty() && viewState.isLoading,
            )
            val relatedSection = if (link == null) {
                null
            } else if (link.relatedCount > 0) {
                emptyList<RelatedLinkUi>() // not supported yet
            } else {
                null
            }

            LinkDetailsUi(
                swipeRefresh = SwipeRefreshUi(
                    isRefreshing = viewState.isLoading && link != null,
                    refreshAction = safeCallback {
                        coroutineScope {
                            viewStateStorage.update { viewState.copy(isLoading = true) }
                            val linkRefresh = async { linkStore.fresh(linkId) }
                            val commentsRefresh = async { commentsStore.fresh(linkId) }

                            runCatching { awaitAll(linkRefresh, commentsRefresh) }
                                .onSuccess {
                                    viewStateStorage.update {
                                        viewState.copy(
                                            isLoading = false,
                                            failedAction = null,
                                            forciblyShownBlockedComments = emptySet(),
                                        )
                                    }
                                }
                                .onFailure { failure ->
                                    viewStateStorage.update {
                                        viewState.copy(
                                            isLoading = false,
                                            failedAction = FailedAction(cause = failure),
                                            forciblyShownBlockedComments = emptySet(),
                                        )
                                    }
                                }
                        }
                    },
                ),
                header = header,
                relatedSection = relatedSection,
                commentsSection = commentsSection,
                errorDialog = viewState.failedAction?.let { error ->
                    ErrorDialogUi(
                        error = error.cause,
                        retryAction = error.retryAction,
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(failedAction = null) } },
                    )
                },
                contextMenuOptions = listOfNotNull(
                    link?.wykopUrl?.let { shareUrl ->
                        ContextMenuOptionUi(
                            option = LinkDetailsMenuOption.Share,
                            onClick = safeCallback { interopRequests.request(InteropRequest.Share(url = shareUrl)) },
                        )
                    },
                ),
                picker = viewState.picker,
            )
        }

    private fun commentsFlow() =
        combine(
            commentsStore.stream(StoreRequest.cached(key = linkId, refresh = false))
                .map { it.dataOrNull() }
                .distinctUntilChanged(),
            getLinksPreferences()
                .map { it.commentsSort }
                .distinctUntilChanged(),
            getFilteringPreferences(),
            getImagePreferences(),
            userInfoStorage.loggedUser,
            detailsFlow().filterNotNull(),
            blacklistStore.stream(StoreRequest.cached(key = Unit, refresh = false))
                .map { it.dataOrNull() }
                .filterNotNull()
                .distinctUntilChanged(),
            viewStateStorage.state,
        ) { comments, sortPreferences, filteringPreferences, imagePreferences, loggedUser, link, blacklist, viewState ->
            val comparator: Comparator<LinkComment> = when (sortPreferences) {
                CommentsDefaultSort.Best -> compareBy { it.totalCount }
                CommentsDefaultSort.New -> compareByDescending { it.postedAt }
                CommentsDefaultSort.Old -> compareBy { it.postedAt }
            }

            val commentsUi = comments.orEmpty()
                .toSortedMap(comparator)
                .mapNotNull { (key, value) ->
                    val isCollapsed = viewState.collapsedIds.contains(key.id)

                    val replies = value
                        .sortedBy { it.postedAt }
                        .map { reply -> reply.toUi(loggedUser, link, filteringPreferences, imagePreferences, blacklist, viewState) }
                        .filterNot { it is LinkCommentUi.Hidden && filteringPreferences.hideBlacklistedContent }
                    val parent = ParentCommentUi(
                        collapsedCount = if (isCollapsed && replies.isNotEmpty()) "+${replies.size}" else null,
                        toggleExpansionStateAction = if (replies.isNotEmpty()) {
                            safeCallback {
                                viewStateStorage.update {
                                    val updated = if (isCollapsed) {
                                        it.collapsedIds - key.id
                                    } else {
                                        it.collapsedIds + key.id
                                    }
                                    it.copy(collapsedIds = updated)
                                }
                            }
                        } else {
                            null
                        },
                        data = key.toUi(loggedUser, link, filteringPreferences, imagePreferences, blacklist, viewState),
                    )

                    parent to replies.takeUnless { isCollapsed }.orEmpty()
                }
                .toMap()
            val allComments = comments.orEmpty().flatMap { it.value.map { it.id } + it.key.id }.toSet()
            val commentsAction = if (viewState.collapsedIds.size == allComments.size) {
                safeCallback { viewStateStorage.update { it.copy(collapsedIds = emptySet()) } }
            } else {
                safeCallback { viewStateStorage.update { it.copy(collapsedIds = allComments) } }
            }
            commentsUi to commentsAction
        }

    private fun detailsFlow() =
        linkStore.stream(StoreRequest.cached(linkId, refresh = false))
            .map { it.dataOrNull() }
            .distinctUntilChanged()

    private suspend fun LinkComment.toUi(
        loggedUser: LoggedUserInfo?,
        link: LinkInfo,
        filteringPreferences: FilteringPreferences,
        imagePreferences: ImagePreferences,
        blacklist: Blacklist,
        viewState: LinkDetailsViewState,
    ): LinkCommentUi {
        val color = when (author.profileId) {
            loggedUser?.id -> ColorConst.CommentCurrentUser
            link.author.profileId -> ColorConst.CommentOriginalPoster
            else -> null
        }
        val isBlocked = isBlocked(filteringPreferences, blacklist)

        return if (isBlocked && !viewState.forciblyShownBlockedComments.contains(id)) {
            LinkCommentUi.Hidden(
                id = id,
                badge = color,
                author = author.toUi(
                    onClicked = null,
                ),
                onClicked = safeCallback {
                    viewStateStorage.update { it.copy(forciblyShownBlockedComments = it.forciblyShownBlockedComments + id) }
                },
            )
        } else {
            val isConsideredNsfw = usedTags.contains("nsfw") && filteringPreferences.hideNsfwContent
            val isConsideredPlus18 = embed?.hasAdultContent == true && filteringPreferences.hidePlus18Content
            val hasNsfwOverlay = isConsideredNsfw || isConsideredPlus18

            LinkCommentUi.Normal(
                id = id,
                postedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(),
                app = app,
                body = body.takeIf { it.isNotBlank() },
                author = author.toUi(
                    onClicked = safeCallback { interopRequests.request(InteropRequest.Profile(profileId = author.profileId)) },
                ),
                plusCount = coloredCounter(
                    userAction = userAction.takeIf { it == UserVote.Up },
                    voteCount = plusCount,
                    onClicked = safeCallback {
                        if (userAction == null) {
                            linksRepository.commentVoteUp(linkId = link.id, commentId = id)
                        } else {
                            linksRepository.removeCommentVote(linkId = link.id, commentId = id)
                        }
                    },
                ),
                minusCount = coloredCounter(
                    userAction = userAction.takeIf { it == UserVote.Down },
                    voteCount = minusCount,
                    onClicked = safeCallback {
                        if (userAction == null) {
                            linksRepository.commentVoteDown(linkId = link.id, commentId = id)
                        } else {
                            linksRepository.removeCommentVote(linkId = link.id, commentId = id)
                        }
                    },
                ),
                badge = color,
                shareAction = safeCallback { interopRequests.request(InteropRequest.Share(url = wykopUrl(linkId = link.id))) },
                embed = embed?.let { embed ->
                    embed.toUi(
                        clickAction = safeCallback {
                            if (hasNsfwOverlay) {
                                TODO("Hide nsfw")
                            } else {
                                showEmbedImage(embed, commentId = id)
                            }
                        },
                        hasNsfwOverlay = hasNsfwOverlay,
                        forceExpanded = false,
                        thresholdPercentage = imagePreferences.cutImagesProportion.takeIf { imagePreferences.cutImages },
                    )
                },
            )
        }
    }

    private suspend fun showEmbedImage(embed: Embed, commentId: Long) {
        when (embed.type) {
            EmbedType.StaticImage -> TODO("Show image")
            EmbedType.AnimatedImage -> TODO("Show animated image")
            EmbedType.Video -> TODO("Show image")
            EmbedType.Unknown -> viewStateStorage.update {
                it.copy(failedAction = FailedAction(IllegalArgumentException("Unsupported image type. commentId=$commentId")))
            }
        }
    }

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safeKeyed<LinkDetailsScope>(linkId) {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(failedAction = FailedAction(failure)) } }
        }
    }
}

private val VoteDownReason.label: String
    get() = when (this) {
        VoteDownReason.Duplicate -> Strings.Link.BURY_REASON_DUPLICATE
        VoteDownReason.Spam -> Strings.Link.BURY_REASON_SPAM
        VoteDownReason.FakeInfo -> Strings.Link.BURY_REASON_FAKE_INFO
        VoteDownReason.WrongContent -> Strings.Link.BURY_REASON_WRONG_CONTENT
        VoteDownReason.UnsuitableContent -> Strings.Link.BURY_REASON_UNSUITABLE_CONTENT
    }

private val CommentsDefaultSort.label: String
    get() = when (this) {
        CommentsDefaultSort.Best -> Strings.Link.COMMENTS_SORT_BEST
        CommentsDefaultSort.New -> Strings.Link.COMMENTS_SORT_NEW
        CommentsDefaultSort.Old -> Strings.Link.COMMENTS_SORT_OLD
    }

private val LinkInfo.upvotePercentage: String?
    get() {
        val sum = voteCount + buryCount

        return if (sum > 0) {
            (100f * voteCount / sum).roundToInt()
        } else {
            null
        }
            ?.let(Strings.Link::upvotesPercentage)
    }
private val tagsRegex by lazy {
    "(^|\\s)#([a-z\\d-]+)".toRegex()
}

private val LinkComment.usedTags
    get() = tagsRegex.matchEntire(body)?.groupValues.orEmpty()

private suspend fun LinkComment.isBlocked(
    filteringPreferences: FilteringPreferences,
    blacklist: Blacklist,
): Boolean = withContext(AppDispatchers.Default) {
    val blockedTagsUsed = usedTags.intersect(blacklist.tags)
    val linkAuthorOnBlackList = blacklist.users.contains(author.profileId)
    val isTooLowRank = filteringPreferences.hideNewUserContent && author.color == UserInfo.Color.Green

    blockedTagsUsed.isNotEmpty() || linkAuthorOnBlackList || isTooLowRank
}
