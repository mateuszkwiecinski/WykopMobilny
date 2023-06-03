package io.github.wykopmobilny.storage.android

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

internal object InstantAdapter : ColumnAdapter<Instant, Long> {

    override fun decode(databaseValue: Long) = Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant) = value.toEpochMilliseconds()
}
