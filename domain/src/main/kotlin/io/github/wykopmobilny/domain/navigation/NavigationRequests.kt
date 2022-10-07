package io.github.wykopmobilny.domain.navigation

import io.github.wykopmobilny.domain.strings.Strings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

interface InteropRequestService {
    val request: Flow<InteropRequest>
}

internal interface InteropRequestsProvider {

    suspend fun request(navigation: InteropRequest)
}

sealed class InteropRequest {
    object BlackListScreen : InteropRequest()
    data class ShowToast(val message: String) : InteropRequest()
    data class Profile(val profileId: String) : InteropRequest()
    data class PrivateMessage(val profileId: String) : InteropRequest()
    data class NewEntry(val profileId: String) : InteropRequest()
    data class Tag(val tagId: String) : InteropRequest()
    data class WebBrowser(val url: String, val force: Boolean = false) : InteropRequest()
    data class UpvotersList(val linkId: Long) : InteropRequest()
    data class DownvotersList(val linkId: Long) : InteropRequest()
    data class Share(
        val url: String,
        val title: String = Strings.SHARE_TITLE,
        val type: Type = Type.TextPlain,
    ) : InteropRequest() {

        enum class Type {
            TextPlain,
        }
    }

    class ShowImage(val url: String) : InteropRequest()
    class ShowGif(val url: String) : InteropRequest()
    class OpenYoutube(val url: String) : InteropRequest()
    class OpenPlayer(val url: String) : InteropRequest()
}

@Singleton
internal class InMemoryInteropRequestService @Inject constructor() : InteropRequestService, InteropRequestsProvider {

    override val request = MutableSharedFlow<InteropRequest>()

    override suspend fun request(navigation: InteropRequest) {
        request.emit(navigation)
    }
}
