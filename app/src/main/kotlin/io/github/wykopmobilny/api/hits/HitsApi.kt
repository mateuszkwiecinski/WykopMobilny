package io.github.wykopmobilny.api.hits

import io.github.wykopmobilny.api.entries.FilteredData
import io.github.wykopmobilny.models.dataclass.Link
import io.reactivex.Single

interface HitsApi {
    fun currentWeek(): Single<FilteredData<Link>>
    fun currentDay(): Single<FilteredData<Link>>
    fun popular(): Single<FilteredData<Link>>
    fun byMonth(year: Int, month: Int): Single<FilteredData<Link>>
    fun byYear(year: Int): Single<FilteredData<Link>>
}
