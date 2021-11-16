package io.github.wykopmobilny.domain.profile

import io.github.wykopmobilny.data.cache.api.GenderEntity
import io.github.wykopmobilny.data.cache.api.UserColorEntity
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.ui.components.widgets.AvatarUi
import io.github.wykopmobilny.ui.components.widgets.Color
import io.github.wykopmobilny.ui.components.widgets.ColorConst
import io.github.wykopmobilny.ui.components.widgets.ColorReference
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi
import kotlinx.datetime.DateTimePeriod

internal const val DEFAULT_PROFILE_BACKGROUND = "https://i.imgur.com/aSm6pSJ.jpg"

internal fun GenderEntity.toGenderDomain() =
    when (this) {
        GenderEntity.Male -> UserInfo.Gender.Male
        GenderEntity.Female -> UserInfo.Gender.Female
    }

internal fun UserColorEntity.toColorDomain() =
    when (this) {
        UserColorEntity.Green -> UserInfo.Color.Green
        UserColorEntity.Orange -> UserInfo.Color.Orange
        UserColorEntity.Claret -> UserInfo.Color.Claret
        UserColorEntity.Admin -> UserInfo.Color.Admin
        UserColorEntity.Banned -> UserInfo.Color.Banned
        UserColorEntity.Deleted -> UserInfo.Color.Deleted
        UserColorEntity.Client -> UserInfo.Color.Client
        UserColorEntity.Unknown -> UserInfo.Color.Unknown
    }

internal fun UserInfo.Gender?.toUi() =
    when (this) {
        UserInfo.Gender.Male -> ColorConst.Male
        UserInfo.Gender.Female -> ColorConst.Female
        null -> ColorConst.Transparent
    }

internal fun UserInfo.Color.toUi(): Color =
    when (this) {
        UserInfo.Color.Green -> ColorConst.UserGreen
        UserInfo.Color.Orange -> ColorConst.UserOrange
        UserInfo.Color.Claret -> ColorConst.UserClaret
        UserInfo.Color.Admin -> ColorReference.Admin
        UserInfo.Color.Banned -> ColorConst.UserBanned
        UserInfo.Color.Deleted -> ColorConst.UserDeleted
        UserInfo.Color.Client -> ColorConst.UserClient
        UserInfo.Color.Unknown -> ColorConst.UserUnknown
    }

internal fun DateTimePeriod.toPrettyString(
    suffix: String = "",
    nowFallback: String = "przed chwilą",
): String {
    val yearsPart = when {
        years == 0 -> ""
        years == 1 -> "1 rok"
        years in 5..21 -> "$years lat"
        years % 10 in 2..4 -> "$years lata"
        else -> "$years lat"
    }
    val monthsPart = if (months == 0) {
        ""
    } else {
        "$months mies."
    }
    val daysPart = when {
        days == 0 || years != 0 -> ""
        days == 1 -> "1 dzień"
        else -> "$days dni"
    }

    @Suppress("ComplexCondition")
    val hoursPart = if (hours == 0 || years != 0 || months != 0 || days != 0) {
        ""
    } else {
        "$hours godz."
    }
    val minutesPart = when {
        years != 0 || months != 0 || days != 0 || hours != 0 -> ""
        minutes > 0 -> "$minutes min"
        else -> return nowFallback
    }

    return buildString {
        yearsPart.takeIf(String::isNotEmpty)?.let(::append)
        monthsPart.takeIf(String::isNotEmpty)?.let {
            append(" ")
            append(it)
        }
        daysPart.takeIf(String::isNotEmpty)?.let {
            append(" ")
            append(it)
        }
        hoursPart.takeIf(String::isNotEmpty)?.let {
            append(" ")
            append(it)
        }
        minutesPart.takeIf(String::isNotEmpty)?.let {
            append(" ")
            append(it)
        }
        append(" ")
        append(suffix)
    }.trim()
}

internal fun LoggedUserInfo.toUi(onClicked: (() -> Unit)?) = UserInfoUi(
    avatar = AvatarUi(
        avatarUrl = avatarUrl,
        rank = null,
        genderStrip = null,
        onClicked = onClicked,
    ),
    name = id,
    color = null,
)
