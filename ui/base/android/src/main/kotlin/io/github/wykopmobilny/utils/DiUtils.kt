package io.github.wykopmobilny.utils

import android.content.Context

inline fun <reified T : Any> Context.requireDependency() =
    (applicationContext as ApplicationInjector).getDependency(T::class)

inline fun <reified T : Any> Context.requireKeyedDependency(key: String) =
    (applicationContext as ApplicationInjector).getDependency(T::class, key)

inline fun <reified T : Any> Context.destroyDependency() =
    (applicationContext as ApplicationInjector).destroyDependency(T::class)

inline fun <reified T : Any> Context.destroyKeyedDependency(key: String) =
    (applicationContext as ApplicationInjector).destroyDependency(T::class, key)
