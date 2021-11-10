package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.unboundedHeight
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.base.components.SwipeRefreshUi
import io.github.wykopmobilny.ui.components.widgets.AvatarUi
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.Color
import io.github.wykopmobilny.ui.components.widgets.ColorConst
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
                                plusCount = plusCounter(),
                                minusCount = minusCounter(),
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
                                plusCount = plusCounter(count = 10),
                                minusCount = minusCounter(count = 10),
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
                                author = stubUser("fixture-parent-user 2", color = ColorConst.UserClaret),
                                postedAgo = "12 min. temu",
                                app = null,
                                body = "Comment body",
                                badge = null,
                                plusCount = plusCounter(),
                                minusCount = minusCounter(clicked = true),
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
                                plusCount = plusCounter(clicked = true),
                                minusCount = minusCounter(count = 0),
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
                                plusCount = plusCounter(count = 123),
                                minusCount = minusCounter(count = 1, clicked = true),
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

private fun plusCounter(
    count: Int = 123,
    clicked: Boolean = false,
) = Button(
    icon = Drawable.Plus,
    label = count.toString(),
    color = if (clicked) ColorConst.CounterUpvoted else null,
    clickAction = {},
)
private fun minusCounter(
    count: Int = 123,
    clicked: Boolean = false,
) = Button(
    icon = Drawable.Minus,
    label = count.toString(),
    color = if (clicked) ColorConst.CounterDownvoted else null,
    clickAction = {},
)
