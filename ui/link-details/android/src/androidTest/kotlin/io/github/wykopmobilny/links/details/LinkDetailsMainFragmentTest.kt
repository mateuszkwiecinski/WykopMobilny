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
                contextMenuOptions = listOf(
                    ContextMenuOptionUi(
                        icon = Drawable.Share,
                        label = "Share",
                        onClick = {},
                    ),
                ),
                picker = null,
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
                                plusCount = stubCounter(),
                                minusCount = stubCounter(),
                                shareAction = {},
                                embed = null,
                            ),
                        ) to listOf(
                            LinkCommentUi.Normal(
                                id = 124,
                                author = stubUser("fixture-reply-user"),
                                postedAgo = "24 godziny temu",
                                app = null,
                                body = "Comment body",
                                badge = ColorConst.CommentOriginalPoster,
                                plusCount = stubCounter(count = 10),
                                minusCount = stubCounter(count = 10),
                                shareAction = {},
                                embed = null,
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
                                plusCount = stubCounter(),
                                minusCount = stubCounter(color = ColorConst.CounterDownvoted),
                                shareAction = {},
                                embed = null,
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
                                plusCount = stubCounter(color = ColorConst.CounterUpvoted),
                                minusCount = stubCounter(count = 0),
                                shareAction = {},
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
                                plusCount = stubCounter(count = 123),
                                minusCount = stubCounter(count = 1, color = ColorConst.CounterDownvoted),
                                shareAction = {},
                                embed = null,
                            ),
                        ),
                    ),
                    isLoading = false,
                ),
                errorDialog = null,
                contextMenuOptions = emptyList(),
                picker = null,
            )
        }
        record(size = unboundedHeight())
    }

    private fun stubUser(
        text: String,
        color: Color = ColorConst.UserOrange,
    ) = UserInfoUi(
        avatar = AvatarUi(
            avatarUrl = null,
            rank = 123,
            genderStrip = ColorConst.Male,
            onClicked = null,
        ),
        name = text,
        color = color,
    )

    private fun stubCounter(
        count: Int = 123,
        color: Color? = null,
    ) = Button(
        label = count.toString(),
        color = color,
        clickAction = {},
    )
}
