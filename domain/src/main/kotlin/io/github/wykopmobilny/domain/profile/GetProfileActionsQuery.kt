package io.github.wykopmobilny.domain.profile

import androidx.paging.Pager
import androidx.paging.map
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.domain.settings.LinkImagePosition
import io.github.wykopmobilny.domain.settings.prefs.GetLinksPreferences
import io.github.wykopmobilny.domain.settings.prefs.LinksPreference
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.kotlin.AppScopes
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.components.widgets.AvatarUi
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.ColorConst
import io.github.wykopmobilny.ui.components.widgets.ListElementUi
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi
import io.github.wykopmobilny.ui.profile.GetProfileActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import javax.inject.Inject

internal class GetProfileActionsQuery @Inject constructor(
    @ProfileId private val profileId: String,
    private val pager: Pager<Int, ProfileAction>,
    private val clock: Clock,
    private val getLinksPreferences: GetLinksPreferences,
    private val appScopes: AppScopes,
    private val interopRequests: InteropRequestsProvider,
) : GetProfileActions {

    override fun invoke() =
        combine(
            pager.flow,
            getLinksPreferences(),
        ) { pagingData, linksPreference ->
            pagingData.map { action ->
                when (action) {
                    is EntryInfo -> action.toUi()
                    is LinkInfo -> action.toUi(linksPreference)
                }
            }
        }

    private fun EntryInfo.toUi() = ListElementUi.Entry(
        id = id,
        body = body,
        voteCount = coloredCounter(
            userAction = userAction,
            voteCount = voteCount,
            onClicked = safeCallback { TODO("voteOnEntry id=$id") },
        ),
        previewImageUrl = previewImageUrl,
        commentsCount = commentsCount,
        author = author.toUi(onClicked = safeCallback { interopRequests.request(InteropRequest.Profile(profileId = profileId)) }),
        addedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
        app = app,
        hasPlus18Overlay = false,
        isFavorite = isFavorite,
        shareAction = safeCallback { },
        favoriteAction = safeCallback { },
        voteAction = safeCallback { },
    )

    private fun LinkInfo.toUi(linksPreference: LinksPreference) =
        ListElementUi.Link(
            id = id,
            title = title,
            body = description,
            previewImageUrl = previewImageUrl,
            commentsCount = commentsCount,
            voteCount = coloredCounter(
                userAction = userAction,
                voteCount = voteCount,
                onClicked = safeCallback { TODO("voteOnLink id=$id") },
            ),
            addedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
            shareAction = safeCallback { },
            favoriteAction = safeCallback { },
            voteAction = safeCallback { },
            thumbnail = if (linksPreference.showLinkThumbnail || linksPreference.useSimpleList) {
                ListElementUi.Link.Thumbnail.None
            } else {
                when (linksPreference.imagePosition) {
                    LinkImagePosition.Left -> ListElementUi.Link.Thumbnail.SmallOnLeft
                    LinkImagePosition.Right -> ListElementUi.Link.Thumbnail.SmallOnRight
                    LinkImagePosition.Top -> ListElementUi.Link.Thumbnail.LargeOnTop
                    LinkImagePosition.Bottom -> ListElementUi.Link.Thumbnail.LargeOnBottom
                }
            },
        )

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safeKeyed<ProfileScope>(profileId, function)
    }
}

internal fun UserInfo.toUi(
    onClicked: (() -> Unit)?,
) = UserInfoUi(
    avatar = AvatarUi(
        avatarUrl = avatarUrl,
        rank = rank,
        genderStrip = gender.toUi(),
        onClicked = onClicked,
    ),
    name = profileId,
    color = color.toUi(),
)

private fun coloredCounter(
    userAction: UserVote?,
    voteCount: Int,
    icon: Drawable? = null,
    onClicked: (() -> Unit)?,
): Button {
    val color = when (userAction) {
        UserVote.Up -> ColorConst.CounterUpvoted
        UserVote.Down -> ColorConst.CounterDownvoted
        null -> null
    }

    return Button(
        label = voteCount.toString(),
        color = color,
        icon = icon,
        clickAction = onClicked,
    )
}
