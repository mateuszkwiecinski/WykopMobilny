package io.github.wykopmobilny.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmbedResponse(
    @field:Json(name = "type") val type: String,
    @Deprecated(
        message = "APIV2 is broken and sends invalid links for gifs",
        replaceWith = ReplaceWith("properUrl", imports = arrayOf("io.github.wykopmobilny.api.responses.properUrl")),
    )
    @field:Json(name = "url")
    internal val invalidUrl: String,
    @field:Json(name = "source") val source: String?,
    @field:Json(name = "preview") val preview: String,
    @field:Json(name = "plus18") val plus18: Boolean,
    @field:Json(name = "size") val size: String?,
    @field:Json(name = "animated") val animated: Boolean,
)

val EmbedResponse.properUrl
    @Suppress("DEPRECATION")
    get() = if (type == "image" && animated) {
        invalidUrl.replace(".jpg", ".gif")
    } else {
        invalidUrl
    }
