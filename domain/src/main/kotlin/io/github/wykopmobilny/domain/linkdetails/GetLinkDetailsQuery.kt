package io.github.wykopmobilny.domain.linkdetails

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.navigation.ClipboardService
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.navigation.WykopTextUtils
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.toPrettyString
import io.github.wykopmobilny.domain.profile.toUi
import io.github.wykopmobilny.domain.profile.wykopUrl
import io.github.wykopmobilny.domain.repositories.LinksRepository
import io.github.wykopmobilny.domain.settings.CommentsDefaultSort
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.prefs.GetFilteringPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetImagesPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetLinksPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetMediaPreferences
import io.github.wykopmobilny.domain.settings.prefs.GetMikroblogPreferences
import io.github.wykopmobilny.domain.settings.prefs.MediaPlayerPreferences
import io.github.wykopmobilny.domain.settings.update
import io.github.wykopmobilny.domain.strings.Strings
import io.github.wykopmobilny.domain.utils.combine
import io.github.wykopmobilny.domain.utils.commentparsing.copyableText
import io.github.wykopmobilny.domain.utils.commentparsing.toCommentBody
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.links.details.CommentsSectionUi
import io.github.wykopmobilny.links.details.GetLinkDetails
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.links.details.LinkDetailsHeaderUi
import io.github.wykopmobilny.links.details.LinkDetailsKey
import io.github.wykopmobilny.links.details.LinkDetailsUi
import io.github.wykopmobilny.links.details.ParentCommentUi
import io.github.wykopmobilny.links.details.RelatedLinkUi
import io.github.wykopmobilny.links.details.RelatedLinksSectionUi
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.Resource
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.InfoDialogUi
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
    private val key: LinkDetailsKey,
    private val linkStore: Store<Long, LinkInfo>,
    private val commentsStore: Store<Long, Map<LinkComment, List<LinkComment>>>,
    private val relatedLinksStore: Store<Long, List<RelatedLink>>,
    private val getLinksPreferences: GetLinksPreferences,
    private val getFilteringPreferences: GetFilteringPreferences,
    private val getImagePreferences: GetImagesPreferences,
    private val getMikroblogPreferences: GetMikroblogPreferences,
    private val getMediaPreferences: GetMediaPreferences,
    private val userInfoStorage: UserInfoStorage,
    private val viewStateStorage: LinkDetailsViewStateStorage,
    private val appScopes: AppScopes,
    private val appStorage: AppStorage,
    private val clock: Clock,
    private val interopRequests: InteropRequestsProvider,
    private val blacklistStore: Store<Unit, Blacklist>,
    private val linksRepository: LinksRepository,
    private val textUtils: WykopTextUtils,
    private val clipboardService: ClipboardService,
) : GetLinkDetails {

    override fun invoke() =
        combine(
            detailsFlow(),
            viewStateStorage.state,
            commentsFlow(),
            relatedLinksFlow(),
            userInfoStorage.loggedUser,
            getLinksPreferences(),
        ) { link, viewState, (comments, toggleCommentsAction), relatedLinks, loggedUser, linkpreferences ->
            val header = if (link == null) {
                LinkDetailsHeaderUi.Loading
            } else {
                LinkDetailsHeaderUi.WithData(
                    title = textUtils.parseHtml(link.title).toString(),
                    body = textUtils.parseHtml(link.description).toString(),
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
                    previewImageUrl = link.fullImageUrl,
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
                                    title = Strings.Link.MORE_TITLE_LINK,
                                    reasons = listOf(
                                        OptionPickerUi.Option(
                                            label = Strings.Link.MORE_OPTION_SHARE,
                                            icon = Drawable.Share,
                                            clickAction = safeCallback { interopRequests.request(InteropRequest.Share(link.wykopUrl)) },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.MORE_OPTION_COPY,
                                            icon = Drawable.Copy,
                                            clickAction = safeCallback {
                                                clipboardService.copy(
                                                    """
                                                        ${link.title.copyableText(textUtils)}
                                                        
                                                        ${link.description.copyableText(textUtils)}
                                                    """.trimIndent(),
                                                )
                                                showSnackbar(Strings.COPIED_TO_CLIPBOARD)
                                            },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.moreOptionUpvotersList(link.voteCount),
                                            icon = Drawable.Upvoters,
                                            clickAction = safeCallback { interopRequests.request(InteropRequest.UpvotersList(link.id)) },
                                        ),
                                        OptionPickerUi.Option(
                                            label = Strings.Link.moreOptionDownvotersList(link.buryCount),
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
                    currentUser = loggedUser?.toUi(onClicked = null),
                    addCommentAction = safeCallback { TODO("Not supported") },
                )
            }
            val commentsSection = CommentsSectionUi(
                comments = comments,
                isLoading = comments.isEmpty() && viewState.generalResource.isLoading,
            )
            val relatedSection = if (link == null) {
                null
            } else if (link.relatedCount > 0) {
                val resource = viewState.relatedResource
                if (resource.isLoading) {
                    RelatedLinksSectionUi.Loading
                } else if (resource.failedAction != null) {
                    RelatedLinksSectionUi.FullWidthError(
                        retryAction = safeCallback { resource.failedAction?.retryAction.let(::checkNotNull).invoke() },
                    )
                } else {
                    val addLinkAction = safeCallback { TODO() }
                    if (!relatedLinks.isNullOrEmpty()) {
                        RelatedLinksSectionUi.WithData(
                            links = relatedLinks.map { related -> related.toUi(linkId = link.id) },
                            addLinkAction = addLinkAction,
                        )
                    } else {
                        RelatedLinksSectionUi.Empty(
                            addLinkAction = addLinkAction,
                        )
                    }
                }
            } else {
                null
            }

            LinkDetailsUi(
                swipeRefresh = SwipeRefreshUi(
                    isRefreshing = viewState.generalResource.isLoading && link != null,
                    refreshAction = safeCallback {
                        coroutineScope {
                            viewStateStorage.update { it.copy(generalResource = Resource.loading()) }
                            val linkRefresh = async { linkStore.fresh(key = key.linkId) }
                            val commentsRefresh = async { commentsStore.fresh(key = key.linkId) }

                            runCatching { awaitAll(linkRefresh, commentsRefresh) }
                                .onSuccess {
                                    viewStateStorage.update {
                                        it.copy(
                                            generalResource = Resource.idle(),
                                            forciblyShownBlockedComments = emptySet(),
                                            allowedNsfwImages = emptySet(),
                                        )
                                    }
                                }
                                .onFailure { failure ->
                                    viewStateStorage.update {
                                        it.copy(
                                            generalResource = Resource.error(FailedAction(cause = failure)),
                                            forciblyShownBlockedComments = emptySet(),
                                            allowedNsfwImages = emptySet(),
                                        )
                                    }
                                }
                        }
                    },
                ),
                header = header,
                relatedSection = relatedSection,
                commentsSection = commentsSection,
                errorDialog = viewState.generalResource.failedAction?.let { error ->
                    ErrorDialogUi(
                        error = error.cause,
                        retryAction = error.retryAction,
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(generalResource = Resource.idle()) } },
                    )
                },
                infoDialog = viewState.spoilerDialog?.let {
                    InfoDialogUi(
                        title = Strings.Comment.SPOILER_DIALOG_TITLE,
                        message = it,
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(spoilerDialog = null) } },
                    )
                },
                contextMenuOptions = listOfNotNull(
                    link?.wykopUrl?.let { shareUrl ->
                        ContextMenuOptionUi(
                            icon = Drawable.Share,
                            label = Strings.SHARE_TITLE,
                            onClick = safeCallback { interopRequests.request(InteropRequest.Share(url = shareUrl)) },
                        )
                    },
                ),
                picker = viewState.picker,
                snackbar = viewState.snackbar,
            )
        }

    private fun RelatedLink.toUi(linkId: Long) = RelatedLinkUi(
        author = author?.toUi(onClicked = null),
        upvotesCount = TwoActionsCounterUi(
            count = voteCount,
            color = when (userVote) {
                UserVote.Up -> ColorConst.CounterUpvoted
                UserVote.Down -> ColorConst.CounterDownvoted
                null -> null
            },
            upvoteAction = when (userVote) {
                UserVote.Up -> null
                UserVote.Down,
                null,
                -> safeCallback { linksRepository.relatedVoteUp(linkId = linkId, relatedId = id) }
            },
            downvoteAction = when (userVote) {
                UserVote.Down -> null
                UserVote.Up,
                null,
                -> safeCallback { linksRepository.relatedVoteDown(linkId = linkId, relatedId = id) }
            },
        ),
        title = title,
        domain = URL(url).host.removePrefix("www."),
        clickAction = safeCallback { interopRequests.request(InteropRequest.WebBrowser(url)) },
        shareAction = safeCallback { interopRequests.request(InteropRequest.Share(url)) },
    )

    private fun commentsFlow() =
        combine(
            commentsStore.stream(StoreRequest.cached(key = key.linkId, refresh = false))
                .map { it.dataOrNull() }
                .distinctUntilChanged(),
            getCommentPreferences(),
            userInfoStorage.loggedUser,
            detailsFlow(),
            blacklistStore.stream(StoreRequest.cached(key = Unit, refresh = false))
                .map { it.dataOrNull() }
                .filterNotNull()
                .distinctUntilChanged(),
            viewStateStorage.state,
        ) { comments, commentPreferences, loggedUser, link, blacklist, viewState ->
            val comparator: Comparator<LinkComment> = when (commentPreferences.sort) {
                CommentsDefaultSort.Best -> compareByDescending { it.totalCount }
                CommentsDefaultSort.New -> compareByDescending { it.postedAt }
                CommentsDefaultSort.Old -> compareBy { it.postedAt }
            }
            link ?: return@combine emptyMap<ParentCommentUi, List<LinkCommentUi>>() to null

            val commentsUi = comments.orEmpty()
                .toSortedMap(comparator.thenComparing(compareBy { it.id }))
                .map { (key, value) ->
                    val isCollapsed = viewState.collapsedIds.contains(key.id)

                    val replies = value
                        .sortedBy { it.postedAt }
                        .map { reply -> reply.toUi(loggedUser, link, commentPreferences, blacklist, viewState) }
                        .filterNot { it is LinkCommentUi.Hidden && commentPreferences.hideBlacklistedContent }
                    val parent = ParentCommentUi(
                        collapsedCount = if (isCollapsed && replies.isNotEmpty()) "+${replies.size}" else null,
                        toggleExpansionStateAction = if (replies.isNotEmpty()) {
                            safeCallback {
                                viewStateStorage.update {
                                    val updated = if (isCollapsed) it.collapsedIds - key.id else it.collapsedIds + key.id
                                    it.copy(collapsedIds = updated)
                                }
                            }
                        } else {
                            null
                        },
                        data = key.toUi(loggedUser, link, commentPreferences, blacklist, viewState),
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

    private fun getCommentPreferences(): Flow<CommentPreferences> = combine(
        getLinksPreferences(),
        getFilteringPreferences(),
        getImagePreferences(),
        getMikroblogPreferences(),
        getMediaPreferences(),
    ) { links, filtering, image, mikroblog, media ->
        CommentPreferences(
            hideNewUserContent = filtering.hideNewUserContent,
            hideNsfwContent = filtering.hideNsfwContent,
            hideBlacklistedContent = filtering.hideBlacklistedContent,
            hidePlus18Content = filtering.hidePlus18Content,
            sort = links.commentsSort,
            openSpoilersInDialog = mikroblog.openSpoilersInDialog,
            showMinifiedImages = image.showMinifiedImages,
            mediaPreferences = media,
        )
    }
        .distinctUntilChanged()

    private fun relatedLinksFlow() =
        relatedLinksStore.stream(StoreRequest.cached(key = key.linkId, refresh = false))
            .map { it.dataOrNull() }

    private fun detailsFlow() =
        linkStore.stream(StoreRequest.cached(key = key.linkId, refresh = false))
            .map { it.dataOrNull() }
            .distinctUntilChanged()

    private suspend fun LinkComment.toUi(
        loggedUser: LoggedUserInfo?,
        link: LinkInfo,
        commentPreferences: CommentPreferences,
        blacklist: Blacklist,
        viewState: LinkDetailsViewState,
    ): LinkCommentUi {
        val color = when {
            id == key.initialCommentId -> ColorConst.CommentLinked
            author.profileId == loggedUser?.id -> ColorConst.CommentCurrentUser
            author.profileId == link.author.profileId -> ColorConst.CommentOriginalPoster
            else -> null
        }
        val isBlocked = isBlocked(commentPreferences.hideNewUserContent, blacklist)

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
            val isConsideredNsfw = usedTags.contains("nsfw") && commentPreferences.hideNsfwContent
            val isConsideredPlus18 = embed?.hasAdultContent == true && commentPreferences.hidePlus18Content
            val couldHaveNsfwOverlay = isConsideredNsfw || isConsideredPlus18
            val hasNsfwOverlay = couldHaveNsfwOverlay && !viewState.allowedNsfwImages.contains(embed?.id)

            LinkCommentUi.Normal(
                id = id,
                postedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
                app = app,
                body = body.toCommentBody(
                    textUtils = textUtils,
                    showsSpoilersInDialog = commentPreferences.openSpoilersInDialog,
                    expandedSpoilers = viewState.expandedSpoilers[id].orEmpty(),
                    showSpoilerDialog = safeCallback { content ->
                        val contentParsed = content()
                        viewStateStorage.update { it.copy(spoilerDialog = contentParsed) }
                    },
                    saveExpandedSpoiler = safeCallback { spoilerId ->
                        viewStateStorage.update {
                            val spoilersInComment = it.expandedSpoilers[id].orEmpty() + spoilerId
                            it.copy(expandedSpoilers = it.expandedSpoilers + (id to spoilersInComment))
                        }
                    },
                    onNavigation = safeCallback { request -> interopRequests.request(request) },
                ),
                author = author.toUi(
                    onClicked = safeCallback { interopRequests.request(InteropRequest.Profile(profileId = author.profileId)) },
                ),
                plusCount = Button(
                    color = if (userAction == UserVote.Up) ColorConst.CounterUpvoted else null,
                    label = plusCount.toString(),
                    icon = Drawable.Plus,
                    clickAction = safeCallback {
                        if (userAction == UserVote.Up) {
                            linksRepository.removeCommentVote(linkId = link.id, commentId = id)
                        } else {
                            linksRepository.commentVoteUp(linkId = link.id, commentId = id)
                        }
                    },
                ),
                minusCount = Button(
                    color = if (userAction == UserVote.Down) ColorConst.CounterDownvoted else null,
                    label = minusCount.toString(),
                    icon = Drawable.Minus,
                    clickAction = safeCallback {
                        if (userAction == UserVote.Down) {
                            linksRepository.removeCommentVote(linkId = link.id, commentId = id)
                        } else {
                            linksRepository.commentVoteDown(linkId = link.id, commentId = id)
                        }
                    },
                ),
                badge = color,
                embed = embed?.let { embed ->
                    embed.toUi(
                        useLowQualityImage = commentPreferences.showMinifiedImages,
                        clickAction = safeCallback {
                            if (hasNsfwOverlay) {
                                viewStateStorage.update { it.copy(allowedNsfwImages = it.allowedNsfwImages + embed.id) }
                            } else {
                                interopRequests.openMedia(
                                    embed = embed,
                                    preferences = commentPreferences.mediaPreferences,
                                    onUnknown = { showError("Unsupported image type. (${embed.id})") },
                                )
                            }
                        },
                        hasNsfwOverlay = hasNsfwOverlay,
                        widthToHeightRatio = embed.ratio,
                    )
                },
                showsOption = viewState.optionsVisibleIds.contains(id),
                favoriteButton = ToggleButtonUi(
                    isToggled = userFavorite,
                    clickAction = safeCallback { linksRepository.toggleCommentFavorite(linkId = link.id, commentId = id) },
                ),
                shareAction = safeCallback { interopRequests.request(InteropRequest.Share(url = wykopUrl(linkId = link.id))) },
                clickAction = safeCallback {
                    viewStateStorage.update {
                        val updated = if (it.optionsVisibleIds.contains(id)) emptySet() else setOf(id)
                        it.copy(optionsVisibleIds = updated)
                    }
                },
                moreAction = safeCallback {
                    viewStateStorage.update { viewState ->
                        viewState.copy(
                            picker = OptionPickerUi(
                                title = Strings.Link.MORE_TITLE_COMMENT,
                                reasons = listOf(
                                    OptionPickerUi.Option(
                                        label = Strings.Link.MORE_OPTION_SHARE,
                                        icon = Drawable.Share,
                                        clickAction = safeCallback {
                                            interopRequests.request(InteropRequest.Share(url = wykopUrl(linkId = link.id)))
                                        },
                                    ),
                                    OptionPickerUi.Option(
                                        label = Strings.Link.MORE_OPTION_COPY,
                                        icon = Drawable.Copy,
                                        clickAction = safeCallback {
                                            clipboardService.copy(body.copyableText(textUtils))
                                            showSnackbar(Strings.COPIED_TO_CLIPBOARD)
                                        },
                                    ),
                                ),
                                dismissAction = safeCallback { viewStateStorage.update { it.copy(picker = null) } },
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun showError(message: String) {
        viewStateStorage.update {
            it.copy(generalResource = Resource.error(failedAction = FailedAction(cause = IllegalArgumentException(message))))
        }
    }

    private suspend fun showSnackbar(copiedToClipboard: String) {
        delay(100)
        viewStateStorage.update { it.copy(snackbar = copiedToClipboard) }
        delay(2500)
        viewStateStorage.update { it.copy(snackbar = null) }
    }

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safeKeyed<LinkDetailsScope>(id = key) {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(generalResource = Resource.error(FailedAction(failure))) } }
        }
    }

    private fun <T> safeCallback(function: suspend CoroutineScope.(T) -> Unit): (T) -> Unit = {
        appScopes.safeKeyed<LinkDetailsScope>(id = key) {
            runCatching { function(it) }
                .onFailure { failure -> viewStateStorage.update { it.copy(generalResource = Resource.error(FailedAction(failure))) } }
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
    hideNewUserContent: Boolean,
    blacklist: Blacklist,
): Boolean = withContext(AppDispatchers.Default) {
    val blockedTagsUsed = usedTags.intersect(blacklist.tags)
    val linkAuthorOnBlackList = blacklist.users.contains(author.profileId)
    val isTooLowRank = hideNewUserContent && author.color == UserInfo.Color.Green

    blockedTagsUsed.isNotEmpty() || linkAuthorOnBlackList || isTooLowRank
}

private data class CommentPreferences(
    val hideNewUserContent: Boolean,
    val hideNsfwContent: Boolean,
    val sort: CommentsDefaultSort,
    val hideBlacklistedContent: Boolean,
    val hidePlus18Content: Boolean,
    val openSpoilersInDialog: Boolean,
    val showMinifiedImages: Boolean,
    val mediaPreferences: MediaPlayerPreferences,
)
