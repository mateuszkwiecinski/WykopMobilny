package io.github.wykopmobilny.ui.modules.tag

import io.github.wykopmobilny.api.tag.TagApi
import io.github.wykopmobilny.base.BasePresenter
import io.github.wykopmobilny.base.Schedulers
import io.github.wykopmobilny.utils.intoComposite
import javax.inject.Inject

class TagActivityPresenter @Inject constructor(
    private val schedulers: Schedulers,
    private val tagApi: TagApi
) : BasePresenter<TagActivityView>() {

    lateinit var tag: String

    fun blockTag() {
        tagApi.block(tag)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                { view?.setObserveState(it) },
                { view?.showErrorDialog(it) }
            )
            .intoComposite(compositeObservable)
    }

    fun unblockTag() {
        tagApi.unblock(tag)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                { view?.setObserveState(it) },
                { view?.showErrorDialog(it) }
            )
            .intoComposite(compositeObservable)
    }

    fun observeTag() {
        tagApi.observe(tag)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                { view?.setObserveState(it) },
                { view?.showErrorDialog(it) }
            )
            .intoComposite(compositeObservable)
    }

    fun unobserveTag() {
        tagApi.unobserve(tag)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe(
                { view?.setObserveState(it) },
                { view?.showErrorDialog(it) }
            )
            .intoComposite(compositeObservable)
    }
}
