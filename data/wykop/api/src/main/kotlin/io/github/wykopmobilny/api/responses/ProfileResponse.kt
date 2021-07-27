package io.github.wykopmobilny.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @field:Json(name = "signup_at") val signupAt: String,
    @field:Json(name = "background") val background: String?,
    @field:Json(name = "is_verified") val isVerified: Boolean?,
    @field:Json(name = "is_observed") val isObserved: Boolean?,
    @field:Json(name = "is_blocked") val isBlocked: Boolean?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "about") val description: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "www") val wwwUrl: String?,
    @field:Json(name = "jabber") val jabberUrl: String?,
    @field:Json(name = "gg") val ggUrl: String?,
    @field:Json(name = "city") val city: String?,
    @field:Json(name = "facebook") val facebookUrl: String?,
    @field:Json(name = "twitter") val twitterUrl: String?,
    @field:Json(name = "instagram") val instagramUrl: String?,
    @field:Json(name = "links_added_count") val linksAddedCount: Int,
    @field:Json(name = "links_published_count") val linksPublishedCount: Int,
    @field:Json(name = "comments_count") val commentsCount: Int?,
    @field:Json(name = "rank") val rank: Int?,
    @field:Json(name = "followers") val followers: Int?,
    @field:Json(name = "following") val following: Int?,
    @field:Json(name = "entries") val entriesCount: Int?,
    @field:Json(name = "entries_comments") val entriesCommentsCount: Int?,
    @field:Json(name = "diggs") val diggsCount: Int?,
    @field:Json(name = "buries") val buriesCount: Int?,
    @field:Json(name = "violation_url") val violationUrl: String?,
    @field:Json(name = "ban") val ban: BanResponse?,
    @field:Json(name = "login") val login: String,
    @field:Json(name = "color") val color: Int,
    @field:Json(name = "sex") val sex: String?,
    @field:Json(name = "avatar") val avatar: String,

)
