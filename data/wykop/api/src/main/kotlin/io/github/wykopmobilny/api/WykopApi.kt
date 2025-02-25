package io.github.wykopmobilny.api

import io.github.wykopmobilny.api.endpoints.AddLinkRetrofitApi
import io.github.wykopmobilny.api.endpoints.EntriesRetrofitApi
import io.github.wykopmobilny.api.endpoints.ExternalRetrofitApi
import io.github.wykopmobilny.api.endpoints.HitsRetrofitApi
import io.github.wykopmobilny.api.endpoints.LinksRetrofitApi
import io.github.wykopmobilny.api.endpoints.LoginRetrofitApi
import io.github.wykopmobilny.api.endpoints.MyWykopRetrofitApi
import io.github.wykopmobilny.api.endpoints.NotificationsRetrofitApi
import io.github.wykopmobilny.api.endpoints.PMRetrofitApi
import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.endpoints.SearchRetrofitApi
import io.github.wykopmobilny.api.endpoints.SuggestRetrofitApi
import io.github.wykopmobilny.api.endpoints.TagRetrofitApi

interface WykopApi {

    fun addLinkRetrofitApi(): AddLinkRetrofitApi

    fun entriesRetrofitApi(): EntriesRetrofitApi

    fun externalRetrofitApi(): ExternalRetrofitApi

    fun hitsRetrofitApi(): HitsRetrofitApi

    fun linksRetrofitApi(): LinksRetrofitApi

    fun loginRetrofitApi(): LoginRetrofitApi

    fun myWykopRetrofitApi(): MyWykopRetrofitApi

    fun notificationsRetrofitApi(): NotificationsRetrofitApi

    fun pMRetrofitApi(): PMRetrofitApi

    fun profileRetrofitApi(): ProfileRetrofitApi

    fun searchRetrofitApi(): SearchRetrofitApi

    fun suggestRetrofitApi(): SuggestRetrofitApi

    fun tagRetrofitApi(): TagRetrofitApi

    fun errorBodyParser(): ErrorBodyParser
}
