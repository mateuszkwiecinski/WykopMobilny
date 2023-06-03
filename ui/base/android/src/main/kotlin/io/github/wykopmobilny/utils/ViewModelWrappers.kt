package io.github.wykopmobilny.utils

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified TDependency : Any> Fragment.viewModelWrapperFactory() =
    object : ViewModelProvider.AndroidViewModelFactory(context?.applicationContext as Application) {

        @kotlin.Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            viewModelWrapper<TDependency>(context?.applicationContext as Application) as T
    }

inline fun <TKey : Any, reified TDependency : Any> Fragment.viewModelWrapperFactoryKeyed(key: TKey) =
    object : ViewModelProvider.AndroidViewModelFactory(context?.applicationContext as Application) {

        @kotlin.Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            viewModelWrapperKeyed<TKey, TDependency>(context?.applicationContext as Application, key) as T
    }

inline fun <TKey : Any, reified TDependency : Any> viewModelWrapperKeyed(application: Application, key: TKey) =
    object : InjectableViewModel<TDependency>(application) {

        override val dependency = getApplication<Application>().requireKeyedDependency<TDependency>(key = key)

        override fun onCleared() {
            super.onCleared()
            getApplication<Application>().destroyKeyedDependency<TDependency>(key = key)
        }
    }

inline fun <reified TDependency : Any> viewModelWrapper(application: Application) = object : InjectableViewModel<TDependency>(application) {

    override val dependency = getApplication<Application>().requireDependency<TDependency>()

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().destroyDependency<TDependency>()
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
