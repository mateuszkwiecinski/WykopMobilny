package io.github.wykopmobilny.domain.settings.prefs

import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.settings.CommentsDefaultSort
import io.github.wykopmobilny.domain.settings.LinkImagePosition
import io.github.wykopmobilny.domain.settings.UpcomingSort
import io.github.wykopmobilny.domain.settings.UserSettings
import io.github.wykopmobilny.domain.settings.get
import io.github.wykopmobilny.domain.utils.combine
import javax.inject.Inject

internal class GetLinksPreferences @Inject constructor(
    private val appStorage: AppStorage,
) {

    operator fun invoke() = combine(
        appStorage.get(UserSettings.useSimpleList),
        appStorage.get(UserSettings.showLinkThumbnail),
        appStorage.get(UserSettings.imagePosition),
        appStorage.get(UserSettings.showAuthor),
        appStorage.get(UserSettings.cutLinkComments),
        appStorage.get(UserSettings.commentsSort),
        appStorage.get(UserSettings.upcomingSort),
    ) { useSimpleList, showLinkThumbnail, imagePosition, showAuthor, cutLinkComments, commentsSort, upcomingSort ->
        LinksPreference(
            useSimpleList = useSimpleList ?: false,
            showLinkThumbnail = showLinkThumbnail ?: true,
            imagePosition = imagePosition ?: LinkImagePosition.Left,
            showAuthor = showAuthor ?: false,
            cutLinkComments = cutLinkComments ?: false,
            commentsSort = commentsSort ?: CommentsDefaultSort.Old,
            upcomingSort = upcomingSort ?: UpcomingSort.Active,
        )
    }
}

internal data class LinksPreference(
    val useSimpleList: Boolean,
    val showLinkThumbnail: Boolean,
    val imagePosition: LinkImagePosition,
    val showAuthor: Boolean,
    val cutLinkComments: Boolean,
    val commentsSort: CommentsDefaultSort,
    val upcomingSort: UpcomingSort,
)
