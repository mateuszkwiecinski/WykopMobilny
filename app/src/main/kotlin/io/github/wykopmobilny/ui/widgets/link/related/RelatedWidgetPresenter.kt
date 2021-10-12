package io.github.wykopmobilny.ui.widgets.link.related

import io.github.wykopmobilny.api.links.LinksApi
import io.github.wykopmobilny.base.BasePresenter
import io.github.wykopmobilny.base.Schedulers
import io.github.wykopmobilny.utils.intoComposite
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import java.lang.Exception

class RelatedWidgetPresenter(
    val schedulers: Schedulers,
    val linksApi: LinksApi,
    val linkHandler: WykopLinkHandler,
) : BasePresenter<RelatedWidgetView>() {

    var relatedId = -1
    var linkId: Long? = null

    fun handleLink(url: String) = linkHandler.handleUrl(url)

    fun voteUp() {
        val linkId = linkId ?: return view?.showErrorDialog(Exception("Sorky, to jeszcze nie dziala")) ?: Unit
        linksApi.relatedVoteUp(linkId, relatedId)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                {
                    it.voteCount?.let { view?.setVoteCount(it) }
                    view?.markVoted()
                },
                { view?.showErrorDialog(it) },
            )
            .intoComposite(compositeObservable)
    }

    fun voteDown() {
        val linkId = linkId ?: return view?.showErrorDialog(Exception("Sorky, to jeszcze nie dziala")) ?: Unit
        linksApi.relatedVoteDown(linkId, relatedId)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                {
                    it.voteCount?.let { view?.setVoteCount(it) }
                    view?.markUnvoted()
                },
                { view?.showErrorDialog(it) },
            )
            .intoComposite(compositeObservable)
    }
}
