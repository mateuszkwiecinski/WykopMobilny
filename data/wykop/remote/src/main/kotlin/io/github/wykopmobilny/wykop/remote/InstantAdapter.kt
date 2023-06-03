package io.github.wykopmobilny.wykop.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val apiTimeZone = TimeZone.of("Europe/Warsaw")

internal class InstantAdapter {

    @ToJson
    fun toJson(date: Instant?) = date?.toString()

    @FromJson
    fun fromJson(date: String) = runCatching { date.replace(' ', 'T').toLocalDateTime() }
        .recoverCatching { date.toLocalDateTime() }
        .onFailure { Napier.e("Could parse $date", it) }
        .getOrNull()
        ?.toInstant(apiTimeZone)
}
