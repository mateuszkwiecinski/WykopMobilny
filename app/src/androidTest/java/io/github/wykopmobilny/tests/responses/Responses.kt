package io.github.wykopmobilny.tests.responses

import io.github.wykopmobilny.tests.rules.MockWebServerRule
import io.github.wykopmobilny.tests.rules.enqueue
import okhttp3.mockwebserver.MockResponse

fun MockWebServerRule.callsOnAppStart() {
    promoted()
    notificationsCountEmpty()
    hashtagsCountEmpty()
    githubPatronsEmpty()
}

fun MockWebServerRule.promoted() = enqueue("/links/promoted/page/1") { jsonResponse("promoted.json") }

fun MockWebServerRule.notificationsCountEmpty() = enqueue("/notifications/Count") { jsonResponse("notificationscount_empty.json") }

fun MockWebServerRule.hashtagsCountEmpty() = enqueue("/notifications/HashTagsCount") { jsonResponse("hashtagscount_empty.json") }

fun MockWebServerRule.githubPatronsEmpty() =
    enqueue("/otwarty-wykop-mobilny/owm-patrons/master/patrons.json") { jsonResponse("githubpatrons_empty.json") }

fun MockWebServerRule.profile() = enqueue("/login/index") { jsonResponse("login.json") }

fun MockWebServerRule.blacklist() = enqueue("/ustawienia/czarne-listy/") { jsonResponse("blacklist.html") }

fun MockWebServerRule.unblockTag(tag: String) = enqueue("/tags/unblock/$tag") { jsonResponse("tagsunblock.json") }

fun MockWebServerRule.unblockUser(user: String) = enqueue("/profiles/unblock/$user") { jsonResponse("profileunblock.json") }

fun MockWebServerRule.connectPage() {
    enqueue("/Login/Connect") {
        MockResponse()
            .setBody("ConnectPage")
            .setResponseCode(302)
            .setHeader("Location", "/ConnectSuccess/appkey/irrelevant/login/fixture-login/token/fixture-token/")
    }
    enqueue("/ConnectSuccess") {
        MockResponse()
            .setBody("ConnectSuccess page content")
    }
}

fun MockWebServerRule.upcomingErrorTwoFactorNeeded() =
    enqueue("/links/upcoming/page/1/sort/comments") { jsonResponse("2fa_required.json", httpCode = 401) }

fun MockWebServerRule.twoFactorAuthSuccess() = enqueue("/login/2fa") { jsonResponse("2fa_success.json") }

fun MockWebServerRule.twoFactorAuthErrorWrongCode() = enqueue("/login/2fa") { jsonResponse("2fa_invalid_code.json") }
