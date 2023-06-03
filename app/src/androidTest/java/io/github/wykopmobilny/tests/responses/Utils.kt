package io.github.wykopmobilny.tests.responses

import io.github.wykopmobilny.tests.rules.MockWebServerRule
import okhttp3.mockwebserver.MockResponse
import okio.Buffer
import java.net.URI

internal fun jsonResponse(filename: String, httpCode: Int = 200) = MockResponse()
    .setResponseCode(httpCode)
    .setBody(
        Reader.readResource(filename).use { stream ->
            stream.reader().readText().replace(LINK_MATCHER) { match ->
                val source = URI(match.value)
                val scheme = if (source.scheme == "https") "http" else source.scheme
                val rewrittenHost = URI(
                    scheme,
                    source.userInfo,
                    "localhost",
                    MockWebServerRule.PORT,
                    source.path,
                    source.query,
                    source.fragment,
                )

                rewrittenHost.toString()
            }
        },
    )

internal fun fileResponse(filename: String, httpCode: Int = 200) = MockResponse()
    .setResponseCode(httpCode)
    .setBody(Reader.readResource(filename).use(Buffer()::readFrom))

private val LINK_MATCHER =
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\/,=]*)".toRegex()

private object Reader

private fun Reader.readResource(name: String) = Reader::class.java.classLoader!!.getResourceAsStream(name)
