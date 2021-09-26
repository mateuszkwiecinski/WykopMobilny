package io.github.wykopmobilny.utils

import kotlin.reflect.KClass

interface ApplicationInjector {

    fun <T : Any> getDependency(clazz: KClass<T>, scopeId: Any? = null): T

    fun <T : Any> destroyDependency(clazz: KClass<T>, scopeId: Any? = null)
}
