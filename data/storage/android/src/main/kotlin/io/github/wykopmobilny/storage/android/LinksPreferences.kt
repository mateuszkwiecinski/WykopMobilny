package io.github.wykopmobilny.storage.android

import android.content.Context
import dagger.Reusable
import io.github.wykopmobilny.storage.api.LinksPreferencesApi
import javax.inject.Inject

@Reusable
internal class LinksPreferences @Inject constructor(
    context: Context,
) : BasePreferences(context), LinksPreferencesApi {

    override var readLinksIds by stringSetPref(key = "readLinksIds")
    override var linkCommentsDefaultSort by stringPref(key = "linkCommentsDefaultSort")
    override var upcomingDefaultSort by stringPref(key = "upcomingDefaultSort")
}
