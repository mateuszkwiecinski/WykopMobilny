package io.github.wykopmobilny.utils

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <TKey : Any, reified TDependency : Any> Fragment.viewModelWrapperFactory(
    key: TKey,
) = object : ViewModelProvider.AndroidViewModelFactory(context?.applicationContext as Application) {

    @kotlin.Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        viewModelWrapper<TKey, TDependency>(context?.applicationContext as Application, key) as T
}

inline fun <TKey : Any, reified TDependency : Any> viewModelWrapper(
    application: Application,
    key: TKey,
) = object : InjectableViewModel<TDependency>(application) {

    override val dependency = getApplication<Application>().requireKeyedDependency<TDependency>(key = key)

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().destroyKeyedDependency<TDependency>(key = key)
    }
}

abstract class InjectableViewModel<TDependency : Any>(
    application: Application,
) : AndroidViewModel(application) {

    abstract val dependency: TDependency

    public override fun onCleared() {
        super.onCleared()
    }
}
