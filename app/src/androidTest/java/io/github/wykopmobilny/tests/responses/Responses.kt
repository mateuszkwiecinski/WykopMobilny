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

fun MockWebServerRule.promoted() =
    enqueue("/links/promoted/page/1") { successfulResponse("promoted.json") }

fun MockWebServerRule.notificationsCountEmpty() =
    enqueue("/notifications/Count") { successfulResponse("notificationscount_empty.json") }

fun MockWebServerRule.hashtagsCountEmpty() =
    enqueue("/notifications/HashTagsCount") { successfulResponse("hashtagscount_empty.json") }

fun MockWebServerRule.githubPatronsEmpty() =
    enqueue("/otwarty-wykop-mobilny/owm-patrons/master/patrons.json") { successfulResponse("githubpatrons_empty.json") }

fun MockWebServerRule.profile() =
    enqueue("/login/index") { successfulResponse("login.json") }

fun MockWebServerRule.blacklist() =
    enqueue("/ustawienia/czarne-listy/") { successfulResponse("blacklist.html") }

fun MockWebServerRule.unblockTag(tag: String) =
    enqueue("/tags/unblock/$tag") { successfulResponse("tagsunblock.json") }

fun MockWebServerRule.unblockUser(user: String) =
    enqueue("/profiles/unblock/$user") { successfulResponse("profileunblock.json") }

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

fun MockWebServerRule.promotedErrorTwoFactorNeeded() {
    enqueue(TODO()) { response("", httpCode = 200) }
}

fun MockWebServerRule.twoFactorAuthSuccess() {
    enqueue("2da_success.json") { successfulResponse("2fa_success.json") }
}
fun MockWebServerRule.twoFactorAuthErrorWrongCode() {
    enqueue("2fa_error.json") { TODO() }
}
