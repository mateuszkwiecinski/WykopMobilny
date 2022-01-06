package io.github.wykopmobilny.wykop.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Reusable
import io.github.wykopmobilny.api.ErrorBodyParser
import io.github.wykopmobilny.api.responses.WykopApiResponse
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import javax.inject.Inject

@Reusable
internal class MoshiErrorBodyParser @Inject constructor(
    private val moshi: Moshi,
) : ErrorBodyParser {

    private val adapter by lazy {
        moshi.adapter<WykopApiResponse<Any>>(Types.newParameterizedType(WykopApiResponse::class.java, Any::class.java))
    }

    override suspend fun parse(body: ResponseBody) = withContext(AppDispatchers.Default) {
        val source = adapter.fromJson(body.source())

        source?.error
    }
}
