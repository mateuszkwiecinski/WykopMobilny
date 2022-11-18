package io.github.wykopmobilny.api.hits

import io.github.wykopmobilny.api.UserTokenRefresher
import io.github.wykopmobilny.api.endpoints.HitsRetrofitApi
import io.github.wykopmobilny.api.errorhandler.ErrorHandlerTransformer
import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.patrons.PatronsApi
import io.github.wykopmobilny.models.mapper.apiv2.filterLinks
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class HitsRepository @Inject constructor(
    private val hitsApi: HitsRetrofitApi,
    private val userTokenRefresher: UserTokenRefresher,
    private val owmContentFilter: OWMContentFilter,
    private val patronsApi: PatronsApi,
) : HitsApi {

    override fun byMonth(year: Int, month: Int) =
        rxSingle { hitsApi.byMonth(year, month) }
            .retryWhen(userTokenRefresher)
            .flatMap { patronsApi.ensurePatrons(it) }
            .compose(ErrorHandlerTransformer())
            .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun currentDay() =
        rxSingle { hitsApi.currentDay() }
            .retryWhen(userTokenRefresher)
            .flatMap { patronsApi.ensurePatrons(it) }
            .compose(ErrorHandlerTransformer())
            .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun byYear(year: Int) =
        rxSingle { hitsApi.byYear(year) }
            .retryWhen(userTokenRefresher)
            .flatMap { patronsApi.ensurePatrons(it) }
            .compose(ErrorHandlerTransformer())
            .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun currentWeek() =
        rxSingle { hitsApi.currentWeek() }
            .retryWhen(userTokenRefresher)
            .flatMap { patronsApi.ensurePatrons(it) }
            .compose(ErrorHandlerTransformer())
            .map { it.filterLinks(owmContentFilter = owmContentFilter) }

    override fun popular() =
        rxSingle { hitsApi.popular() }
            .retryWhen(userTokenRefresher)
            .flatMap { patronsApi.ensurePatrons(it) }
            .compose(ErrorHandlerTransformer())
            .map { it.filterLinks(owmContentFilter = owmContentFilter) }
}
