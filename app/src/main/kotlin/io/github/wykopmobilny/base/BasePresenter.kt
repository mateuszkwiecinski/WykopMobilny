package io.github.wykopmobilny.base

import io.reactivex.disposables.CompositeDisposable

open class BasePresenter<T : BaseView> {

    val compositeObservable = CompositeDisposable()
    var view: T? = null
    val isSubscribed: Boolean
        get() = view != null

    fun subscribe(view: T) {
        this.view = view
    }

    fun unsubscribe() {
        view = null
        compositeObservable.clear()
    }
}
