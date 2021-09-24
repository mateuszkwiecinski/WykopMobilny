package io.github.wykopmobilny.domain.profile

import androidx.paging.Pager
import androidx.paging.map
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.settings.LinkImagePosition
import io.github.wykopmobilny.domain.settings.prefs.GetLinksPreferences
import io.github.wykopmobilny.domain.settings.prefs.LinksPreference
import io.github.wykopmobilny.ui.components.links.ListElementUi
import io.github.wykopmobilny.ui.components.links.UpvoteCounter
import io.github.wykopmobilny.ui.components.users.AvatarUi
import io.github.wykopmobilny.ui.components.users.ColorReference
import io.github.wykopmobilny.ui.components.users.UserInfoUi
import io.github.wykopmobilny.ui.profile.GetProfileActions
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import javax.inject.Inject

internal class GetProfileActionsQuery @Inject constructor(
    private val pager: Pager<Int, ProfileAction>,
    private val clock: Clock,
    private val getLinksPreferences: GetLinksPreferences,
) : GetProfileActions {

    override fun invoke() =
        combine(
            pager.flow,
            getLinksPreferences(),
        ) { pagingData, linksPreference ->
            pagingData.map { action ->
                when (action) {
                    is ProfileAction.Entry -> action.toUi()
                    is ProfileAction.Link -> action.toUi(linksPreference)
                }
            }
        }

    private fun ProfileAction.Entry.toUi() = ListElementUi.Entry(
        id = id,
        body = body,
        voteCount = voteCounter(userAction, voteCount),
        previewImageUrl = previewImageUrl,
        commentsCount = commentsCount,
        author = author.toUi(),
        addedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
        app = app,
        hasPlus18Overlay = false,
        isFavorite = isFavorite,
        shareAction = { },
        favoriteAction = { },
        voteAction = { },
    )

    private fun ProfileAction.Link.toUi(linksPreference: LinksPreference) =
        ListElementUi.Link(
            id = id,
            title = title,
            body = description,
            previewImageUrl = previewImageUrl,
            commentsCount = commentsCount,
            voteCount = voteCounter(userAction, voteCount),
            addedAgo = postedAt.periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString(suffix = "temu"),
            shareAction = { },
            favoriteAction = { },
            voteAction = { },
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
}

internal fun UserInfo.toUi() = UserInfoUi(
    avatar = AvatarUi(
        avatarUrl = avatarUrl,
        rank = rank,
        genderStrip = gender.toUi(),
    ),
    name = profileId,
    color = color.toUi(),
)

internal fun voteCounter(userAction: UserVote?, voteCount: Int): UpvoteCounter {
    val color = when (userAction) {
        UserVote.Up -> ColorReference.CounterUpvoted
        UserVote.Down -> ColorReference.CounterDownvoted
        null -> ColorReference.CounterDefault
    }

    return UpvoteCounter(
        count = voteCount,
        color = color,
    )
}
