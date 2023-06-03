package io.github.wykopmobilny.ui.blacklist.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.blacklist.BlacklistedDetailsUi
import io.github.wykopmobilny.ui.blacklist.GetBlacklistDetails
import kotlinx.coroutines.flow.flowOf

internal fun BaseScreenshotTest.registerBlacklist(blacklist: () -> BlacklistedDetailsUi = { error("unsupported") }) =
    registerDependencies<BlacklistDependencies>(
        object : BlacklistDependencies {

            override fun blacklistDetails() = object : GetBlacklistDetails {
                override fun invoke() = flowOf(blacklist())
            }
        },
    )
