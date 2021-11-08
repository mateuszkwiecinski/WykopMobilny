package io.github.wykopmobilny.data.cache.sqldelight

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.data.cache.api.EntryEntity
import io.github.wykopmobilny.data.cache.api.GenderEntity
import io.github.wykopmobilny.data.cache.api.LinkCommentsEntity
import io.github.wykopmobilny.data.cache.api.LinkEntity
import io.github.wykopmobilny.data.cache.api.ProfileEntity
import io.github.wykopmobilny.data.cache.api.RelatedLinkEntity
import io.github.wykopmobilny.data.cache.api.UserColorEntity
import io.github.wykopmobilny.data.cache.api.UserVote
import kotlinx.datetime.Instant
import javax.inject.Singleton

@Module
internal class AppCacheModule {

    @Singleton
    @Provides
    fun database(context: Context) = AppCache(
        driver = AndroidSqliteDriver(
            schema = AppCache.Schema,
            context = context,
            name = null,
            callback = object : AndroidSqliteDriver.Callback(AppCache.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA foreign_keys=ON;")
                }
            },
        ),
        profileEntityAdapter = ProfileEntity.Adapter(
            signupAtAdapter = InstantAdapter,
            colorAdapter = EnumAdapter(UserColorEntity.values()),
            genderAdapter = EnumAdapter(GenderEntity.values()),
        ),
        linkEntityAdapter = LinkEntity.Adapter(
            postedAtAdapter = InstantAdapter,
            userVoteAdapter = EnumAdapter(UserVote.values()),
        ),
        entryEntityAdapter = EntryEntity.Adapter(
            postedAtAdapter = InstantAdapter,
            userVoteAdapter = EnumAdapter(UserVote.values()),
        ),
        embedAdapter = Embed.Adapter(
            typeAdapter = EnumAdapter(EmbedType.values()),
        ),
        linkCommentsEntityAdapter = LinkCommentsEntity.Adapter(
            postedAtAdapter = InstantAdapter,
            userVoteAdapter = EnumAdapter(UserVote.values()),
        ),
        relatedLinkEntityAdapter = RelatedLinkEntity.Adapter(
            userVoteAdapter = EnumAdapter(UserVote.values()),
        ),
    )
}

internal object InstantAdapter : ColumnAdapter<Instant, Long> {

    override fun decode(databaseValue: Long) =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant) =
        value.toEpochMilliseconds()
}

class EnumAdapter<T : Enum<T>>(private val values: Array<T>) : ColumnAdapter<T, Long> {

    override fun decode(databaseValue: Long) =
        values[databaseValue.toInt()]

    override fun encode(value: T) =
        value.ordinal.toLong()
}
