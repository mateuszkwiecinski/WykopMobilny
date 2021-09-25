package io.github.wykopmobilny.domain.profile

import io.github.wykopmobilny.data.cache.api.GenderEntity
import io.github.wykopmobilny.data.cache.api.UserColorEntity
import io.github.wykopmobilny.ui.components.users.ColorHex
import io.github.wykopmobilny.ui.components.users.ColorReference
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
        UserInfo.Gender.Male -> "#46ABF2"
        UserInfo.Gender.Female -> "#f246d0"
        null -> "#00000000"
    }.let(::ColorHex)

internal fun UserInfo.Color.toUi() =
    when (this) {
        UserInfo.Color.Green -> ColorHex("#339933")
        UserInfo.Color.Orange -> ColorHex("#ff5917")
        UserInfo.Color.Claret -> ColorHex("#BB0000")
        UserInfo.Color.Admin -> ColorReference.Admin
        UserInfo.Color.Banned -> ColorHex("#999999")
        UserInfo.Color.Deleted -> ColorHex("#999999")
        UserInfo.Color.Client -> ColorHex("#3F6FA0")
        UserInfo.Color.Unknown -> ColorHex("#0000FF")
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

    return "$yearsPart $monthsPart $daysPart $hoursPart $minutesPart $suffix".trim()
}
