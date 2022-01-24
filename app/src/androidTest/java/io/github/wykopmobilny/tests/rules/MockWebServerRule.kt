package io.github.wykopmobilny.tests.rules

import io.github.wykopmobilny.tests.responses.fileResponse
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockWebServerRule : TestRule {

    private val mockWebServer = MockWebServer()

    private val dispatcher = MockDispatcher()

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                mockWebServer.dispatcher = dispatcher
                mockWebServer.start(port = PORT)
                try {
                    val result = runCatching { base.evaluate() }
                    dispatcher.unmatchedRequest?.let { error("Failed to match response at path=${dispatcher.unmatchedRequest?.requestUrl?.toString()}") }
                    result.getOrThrow()
                } finally {
                    mockWebServer.shutdown()
                }
            }
        }

    fun enqueue(
        requestMatcher: (RecordedRequest) -> Boolean,
        response: () -> MockResponse,
    ) {
        dispatcher.requests.add(requestMatcher to response)
    }

    companion object {
        const val PORT = 8000
    }
}

private class MockDispatcher : Dispatcher() {
    val requests = mutableListOf<Pair<(RecordedRequest) -> Boolean, () -> MockResponse>>()
    var unmatchedRequest: RecordedRequest? = null

    private val predefinedRequests = listOf(
        pathMatcher("/favicon.ico") to { fileResponse("avatar.png") },
        cdnMatcher() to { fileResponse("avatar.png") },
        pathMatcher("/") to { MockResponse().setResponseCode(500) },
    )

    override fun dispatch(request: RecordedRequest): MockResponse {
        val enqueued =
            requests.firstOrNull { (requestMatcher, _) -> requestMatcher(request) }?.also { requests.remove(it) }
                ?: predefinedRequests.firstOrNull { (requestMatcher, _) -> requestMatcher(request) }
                ?: return MockResponse().setResponseCode(400).also { unmatchedRequest = request }

        return enqueued.second()
    }
}

private fun pathMatcher(path: String): (RecordedRequest) -> Boolean =
    { it.path?.substringBefore("/appkey/") == path }

private fun cdnMatcher(): (RecordedRequest) -> Boolean =
    { it.path?.startsWith("/cdn/") == true }

fun MockWebServerRule.enqueue(
    path: String,
    response: () -> MockResponse,
) = enqueue(pathMatcher(path), response)
