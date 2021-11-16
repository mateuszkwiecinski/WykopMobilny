package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import kotlinx.coroutines.flow.flowOf

internal fun BaseScreenshotTest.registerLinkDetails(
    scopeId: Long = 1,
    block: () -> LinkDetailsUi = { error("unsupported") },
) = registerDependencies<LinkDetailsDependencies>(
    scopeId = LinkDetailsKey(linkId = scopeId, initialCommentId = null).toString(),
    dependency = object : LinkDetailsDependencies {
        override fun getLinkDetails(): GetLinkDetails = object : GetLinkDetails {
            override fun invoke() = flowOf(block())
        }
    },
)
