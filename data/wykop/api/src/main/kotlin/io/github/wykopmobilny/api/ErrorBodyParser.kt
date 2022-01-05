package io.github.wykopmobilny.api

import io.github.wykopmobilny.api.responses.WykopErrorResponse
import okhttp3.ResponseBody

interface ErrorBodyParser {

    suspend fun parse(body: ResponseBody): WykopErrorResponse?
}
