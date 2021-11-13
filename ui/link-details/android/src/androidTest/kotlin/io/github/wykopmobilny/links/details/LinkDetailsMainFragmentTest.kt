package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.loremIpsum
import io.github.wykopmobilny.screenshots.unboundedHeight
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import io.github.wykopmobilny.ui.components.widgets.AvatarUi
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.Color
import io.github.wykopmobilny.ui.components.widgets.ColorConst
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi
import org.junit.Test

internal class LinkDetailsMainFragmentTest : BaseScreenshotTest() {

    override fun createFragment() = linkDetailsFragment(
        linkId = 1,
        commentId = null,
    )

    @Test
    fun allLoading() {
        registerLinkDetails {
            LinkDetailsUi(
                swipeRefresh = SwipeRefreshUi(
                    isRefreshing = false,
                    refreshAction = { },
                ),
                header = LinkDetailsHeaderUi.Loading,
                relatedSection = null,
                commentsSection = CommentsSectionUi(
                    comments = emptyMap(),
                    isLoading = true,
                ),
                errorDialog = null,
                infoDialog = null,
                contextMenuOptions = listOf(
                    ContextMenuOptionUi(
                        icon = Drawable.Share,
                        label = "Share",
                        onClick = {},
                    ),
                ),
                picker = null,
                snackbar = null,
            )
        }
        record(size = unboundedHeight())
    }

    @Test
    fun headerLoading() {
        registerLinkDetails {
            LinkDetailsUi(
                swipeRefresh = SwipeRefreshUi(
                    isRefreshing = false,
                    refreshAction = { },
                ),
                header = LinkDetailsHeaderUi.Loading,
                relatedSection = null,
                commentsSection = CommentsSectionUi(
                    comments = mapOf(
                        ParentCommentUi(
                            collapsedCount = "+124",
                            toggleExpansionStateAction = {},
                            data = LinkCommentUi.Normal(
                                id = 123,
                                author = stubUser("fixture-parent-user 1", color = ColorConst.UserClaret),
                                postedAgo = "12 min. temu",
                                app = null,
                                body = "Comment body",
                                badge = null,
                                plusCount = stubPlusCounter(),
                                minusCount = stubMinusCounter(),
                                embed = null,
                                moreAction = {},
                            ),
                        ) to listOf(
                            LinkCommentUi.Normal(
                                id = 124,
                                author = stubUser("fixture-reply-user"),
                                postedAgo = "24 godz. temu",
                                app = null,
                                body = "Comment body",
                                badge = ColorConst.CommentOriginalPoster,
                                plusCount = stubPlusCounter(count = 10),
                                minusCount = stubMinusCounter(count = 10),
                                embed = null,
                                moreAction = {},
                            ),
                            LinkCommentUi.Hidden(
                                id = 125,
                                author = stubUser("fixture-user-1"),
                                onClicked = {},
                                badge = ColorConst.CommentCurrentUser,
                            ),
                            LinkCommentUi.Hidden(
                                id = 126,
                                author = stubUser("fixture-user-2"),
                                onClicked = {},
                                badge = null,
                            ),
                        ),
                        ParentCommentUi(
                            collapsedCount = "1",
                            toggleExpansionStateAction = {},
                            data = LinkCommentUi.Normal(
                                id = 127,
                                author = stubUser("very_long_user_name_most_likely_more_than_single_line"),
                                postedAgo = "12 years 4 months ago",
                                app = "Wykop the best app long name",
                                body = "Comment body",
                                badge = null,
                                plusCount = stubPlusCounter(),
                                minusCount = stubMinusCounter(clicked = true),
                                embed = null,
                                moreAction = {},
                            ),
                        ) to emptyList(),
                        ParentCommentUi(
                            collapsedCount = null,
                            toggleExpansionStateAction = {},
                            data = LinkCommentUi.Normal(
                                id = 128,
                                author = stubUser("fixture-parent-user", color = ColorConst.UserBanned),
                                postedAgo = "12 min. temu",
                                app = "Random app",
                                body = "Comment body",
                                badge = ColorConst.CommentCurrentUser,
                                plusCount = stubPlusCounter(clicked = true),
                                minusCount = stubMinusCounter(count = 0),
                                moreAction = {},
                                embed = null,
                            ),
                        ) to listOf(
                            LinkCommentUi.Normal(
                                id = 129,
                                author = stubUser("fixture-reply-user 3"),
                                postedAgo = "24 godziny temu",
                                app = null,
                                body = "Comment body",
                                badge = ColorConst.CommentOriginalPoster,
                                plusCount = stubPlusCounter(count = 123),
                                minusCount = stubMinusCounter(count = 1, clicked = true),
                                moreAction = {},
                                embed = null,
                            ),
                            LinkCommentUi.Normal(
                                id = 130,
                                author = stubUser("very_long_user_name_most_likely_more_than_single_line"),
                                postedAgo = "6 years 4 months ago",
                                app = "Wykop the best app long name",
                                body = loremIpsum(30),
                                badge = null,
                                plusCount = stubPlusCounter(count = 9999),
                                minusCount = stubMinusCounter(count = 9999, clicked = true),
                                moreAction = {},
                                embed = null,
                            ),
                            LinkCommentUi.Normal(
                                id = 131,
                                author = stubUser("fixture-reply-user 3"),
                                postedAgo = "24 godziny temu",
                                app = null,
                                body = "has gif image",
                                badge = null,
                                plusCount = stubPlusCounter(count = 123),
                                minusCount = stubMinusCounter(count = 1, clicked = true),
                                moreAction = {},
                                embed = stubEmbedPlayable(
                                    domain = "fixture",
                                    size = "204KB",
                                    hasNsfwOverlay = false,
                                ),
                            ),
                            LinkCommentUi.Normal(
                                id = 131,
                                author = stubUser("fixture-reply-user 3"),
                                postedAgo = "24 godziny temu",
                                app = null,
                                body = "has nsfw image",
                                badge = null,
                                plusCount = stubPlusCounter(count = 10, clicked = true),
                                minusCount = stubMinusCounter(count = 10),
                                moreAction = {},
                                embed = stubEmbedStatic(
                                    size = null,
                                    hasNsfwOverlay = true,
                                ),
                            ),
                            LinkCommentUi.Normal(
                                id = 131,
                                author = stubUser("embed__only", color = ColorConst.UserClaret),
                                postedAgo = "przed chwilą",
                                app = null,
                                body = null,
                                badge = null,
                                plusCount = stubPlusCounter(count = 10),
                                minusCount = stubMinusCounter(count = 10, clicked = true),
                                moreAction = {},
                                embed = stubEmbedStatic(
                                    url = avatarUrl,
                                ),
                            ),
                            LinkCommentUi.Normal(
                                id = 132,
                                author = stubUser("fixture-reply-user 3"),
                                postedAgo = "24 godziny temu",
                                app = null,
                                body = "Comment body",
                                badge = ColorConst.CommentOriginalPoster,
                                plusCount = stubPlusCounter(count = 123),
                                minusCount = stubMinusCounter(count = 1, clicked = true),
                                moreAction = {},
                                embed = null,
                            ),
                        ),
                    ),
                    isLoading = false,
                ),
                errorDialog = null,
                infoDialog = null,
                contextMenuOptions = emptyList(),
                picker = null,
                snackbar = null,
            )
        }
        record(size = unboundedHeight())
    }
}

private fun stubUser(
    text: String,
    color: Color = ColorConst.UserOrange,
) = UserInfoUi(
    avatar = AvatarUi(
        avatarUrl = BaseScreenshotTest.avatarUrl,
        rank = 123,
        genderStrip = ColorConst.Male,
        onClicked = null,
    ),
    name = text,
    color = color,
)

private fun stubPlusCounter(
    count: Int = 123,
    clicked: Boolean = false,
) = Button(
    icon = Drawable.Plus,
    label = count.toString(),
    color = if (clicked) ColorConst.CounterUpvoted else null,
    clickAction = {},
)

private fun stubMinusCounter(
    count: Int = 123,
    clicked: Boolean = false,
) = Button(
    icon = Drawable.Minus,
    label = count.toString(),
    color = if (clicked) ColorConst.CounterDownvoted else null,
    clickAction = {},
)

private fun stubEmbedStatic(
    url: String = BaseScreenshotTest.avatarUrl,
    size: String? = null,
    hasNsfwOverlay: Boolean = false,
) = EmbedMediaUi(
    content = EmbedMediaUi.Content.StaticImage(
        url = url,
    ),
    size = size,
    hasNsfwOverlay = hasNsfwOverlay,
    widthToHeightRatio = 1f,
    clickAction = {},
)

private fun stubEmbedPlayable(
    url: String = BaseScreenshotTest.avatarUrl,
    domain: String = "fixture-domain",
    size: String? = null,
    hasNsfwOverlay: Boolean = false,
) = EmbedMediaUi(
    content = EmbedMediaUi.Content.PlayableMedia(
        previewImage = url,
        domain = domain,
    ),
    size = size,
    hasNsfwOverlay = hasNsfwOverlay,
    widthToHeightRatio = 1f,
    clickAction = {},
)
