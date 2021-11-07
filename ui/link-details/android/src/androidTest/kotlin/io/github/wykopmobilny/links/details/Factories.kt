package io.github.wykopmobilny.links.details

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun BaseScreenshotTest.registerLinkDetails(
    scopeId: Long = 1,
    blacklist: () -> LinkDetailsUi = { error("unsupported") },
) = registerDependencies<LinkDetailsDependencies>(
    scopeId = scopeId.toString(),
    dependency = object : LinkDetailsDependencies {
        override fun getLinkDetails(): GetLinkDetails = object: GetLinkDetails {
            override fun invoke(): Flow<LinkDetailsUi> = flowOf(blacklist())
        }
    },
)
