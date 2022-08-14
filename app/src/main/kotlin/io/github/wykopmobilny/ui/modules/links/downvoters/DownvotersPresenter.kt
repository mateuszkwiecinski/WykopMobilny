package io.github.wykopmobilny.ui.modules.links.downvoters

import io.github.wykopmobilny.api.links.LinksApi
import io.github.wykopmobilny.base.BasePresenter
import io.github.wykopmobilny.base.Schedulers
import io.github.wykopmobilny.utils.intoComposite
import javax.inject.Inject

class DownvotersPresenter @Inject constructor(
    private val schedulers: Schedulers,
    private val linksApi: LinksApi,
) : BasePresenter<DownvotersView>() {

    var linkId = -1L

    fun getDownvoters() {
        linksApi.getDownvoters(linkId)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe({ view?.showDownvoters(it) }, { view?.showErrorDialog(it) })
            .intoComposite(compositeObservable)
    }
}
