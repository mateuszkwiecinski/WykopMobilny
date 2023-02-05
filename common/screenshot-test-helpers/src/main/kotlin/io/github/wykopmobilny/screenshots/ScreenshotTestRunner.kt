package io.github.wykopmobilny.screenshots

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.karumi.shot.ShotTestRunner
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.utils.ApplicationInjector
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

class ScreenshotTestRunner : ShotTestRunner() {

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, ScreenshotTestsApplication::class.java.name, context)
}

class ScreenshotTestsApplication : Application(), ApplicationInjector {

    private val dependencies = mutableMapOf<String, Any>()

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        AppDispatchers.replaceDispatchers(
            io = Dispatchers.Main,
            default = Dispatchers.Main,
        )
    }

    override fun <T : Any> destroyDependency(clazz: KClass<T>, scopeId: Any?) = Unit

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getDependency(clazz: KClass<T>, scopeId: Any?): T =
        runCatching { dependencies[id(clazz, scopeId)] as T }
            .getOrElse { error("Can't find ${id(clazz, scopeId)} in ${dependencies.keys}") }

    fun <T : Any> registerDependency(clazz: KClass<T>, scopeId: Any? = null, dependency: T) {
        dependencies[id(clazz, scopeId)] = dependency
    }

    private fun <T : Any> id(clazz: KClass<T>, scopeId: Any?) =
        scopeId?.let { "[${clazz.java.name}]$it" } ?: clazz.java.name
}
