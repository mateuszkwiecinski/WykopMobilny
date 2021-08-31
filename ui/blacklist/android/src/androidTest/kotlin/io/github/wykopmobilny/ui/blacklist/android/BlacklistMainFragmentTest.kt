package io.github.wykopmobilny.ui.blacklist.android

import androidx.viewpager2.widget.ViewPager2
import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.screenshots.loremIpsum
import io.github.wykopmobilny.ui.blacklist.BlacklistedDetailsUi
import io.github.wykopmobilny.ui.blacklist.BlacklistedElementUi
import org.junit.Test

internal class BlacklistMainFragmentTest : BaseScreenshotTest() {

    override fun createFragment() = BlacklistMainFragment()

    @Test
    fun progress() {
        registerBlacklist(
            blacklist = {
                BlacklistedDetailsUi(
                    errorDialog = null,
                    content = BlacklistedDetailsUi.Content.Empty(
                        isLoading = true,
                        loadAction = {},
                    ),
                )
            },
        )
        record()
    }

    @Test
    fun empty() {
        registerBlacklist(
            blacklist = {
                BlacklistedDetailsUi(
                    errorDialog = null,
                    content = BlacklistedDetailsUi.Content.Empty(
                        isLoading = false,
                        loadAction = {},
                    ),
                )
            },
        )
        record()
    }

    @Test
    fun tagsTab() {
        registerBlacklist(
            blacklist = {
                BlacklistedDetailsUi(
                    errorDialog = null,
                    content = BlacklistedDetailsUi.Content.WithData(
                        tags = BlacklistedDetailsUi.Content.WithData.ElementPage(
                            isRefreshing = false,
                            refreshAction = {},
                            elements = listOf(
                                BlacklistedElementUi(
                                    name = "fixture-tag-1",
                                    state = BlacklistedElementUi.StateUi.Default(
                                        unblock = {},
                                    ),
                                ),
                                BlacklistedElementUi(
                                    name = "fixture-tag-2",
                                    state = BlacklistedElementUi.StateUi.InProgress,
                                ),
                                BlacklistedElementUi(
                                    name = loremIpsum(20),
                                    state = BlacklistedElementUi.StateUi.Error(
                                        showError = {},
                                    ),
                                ),
                            ),
                        ),
                        users = BlacklistedDetailsUi.Content.WithData.ElementPage(
                            isRefreshing = false,
                            refreshAction = {},
                            elements = emptyList(),
                        ),
                    ),
                )
            },
        )
        record()
    }

    @Test
    fun usersTab() {
        registerBlacklist(
            blacklist = {
                BlacklistedDetailsUi(
                    errorDialog = null,
                    content = BlacklistedDetailsUi.Content.WithData(
                        tags = BlacklistedDetailsUi.Content.WithData.ElementPage(
                            isRefreshing = false,
                            refreshAction = {},
                            elements = emptyList(),
                        ),
                        users = BlacklistedDetailsUi.Content.WithData.ElementPage(
                            isRefreshing = false,
                            refreshAction = {},
                            elements = listOf(
                                BlacklistedElementUi(
                                    name = "fixture-user-1",
                                    state = BlacklistedElementUi.StateUi.Default(
                                        unblock = {},
                                    ),
                                ),
                                BlacklistedElementUi(
                                    name = "fixture-user-2",
                                    state = BlacklistedElementUi.StateUi.InProgress,
                                ),
                                BlacklistedElementUi(
                                    name = loremIpsum(20),
                                    state = BlacklistedElementUi.StateUi.Error(
                                        showError = {},
                                    ),
                                ),
                            ),
                        ),
                    ),
                )
            },
        )
        record(
            beforeScreenshot = { findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(1, false) },
        )
    }
}
