package io.github.wykopmobilny.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.r0adkll.slidr.attachSlidr
import com.r0adkll.slidr.model.SlidrConfig
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.github.wykopmobilny.R
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.styles.ApplicableStyleUi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.ui.dialogs.showExceptionDialog
import io.github.wykopmobilny.utils.requireDependency
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * This class should be extended in all activities in this app. Place global-activity settings here.
 */
abstract class BaseActivity : AppCompatActivity(), HasAndroidInjector {

    open val enableSwipeBackLayout: Boolean = false
    open val isActivityTransfluent: Boolean = false
    var isRunning = false

    @Inject
    lateinit var themeSettingsPreferences: SettingsPreferencesApi

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        val getAppStyle = application.requireDependency<StylesDependencies>().getAppStyle()
        val initialStyle = runBlocking { getAppStyle().first() }.style
        initTheme(initialStyle)
        super.onCreate(savedInstanceState)

        val slidr = if (enableSwipeBackLayout) {
            attachSlidr(SlidrConfig(edgeOnly = true))
        } else {
            null
        }
        lifecycleScope.launchWhenResumed {
            val shared = getAppStyle().stateIn(this)
            launch {
                shared
                    .map { it.style }
                    .distinctUntilChanged()
                    .dropWhile { it == initialStyle }
                    .collect {
                        updateTheme(it)
                        recreate()
                    }
            }
            launch {
                shared
                    .map { it.edgeSlidingBehaviorEnabled }
                    .distinctUntilChanged()
                    .collect { isEnabled ->
                        if (isEnabled) {
                            slidr?.unlock()
                        } else {
                            slidr?.lock()
                        }
                    }
            }
        }
    }

    override fun onResume() {
        isRunning = true
        super.onResume()
    }

    override fun onPause() {
        isRunning = false
        super.onPause()
    }

    // This function initializes activity theme based on settings
    private fun initTheme(style: ApplicableStyleUi) {
        updateTheme(style)
        if (isActivityTransfluent || enableSwipeBackLayout) {
            theme.applyStyle(R.style.TransparentActivityTheme, true)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        when (themeSettingsPreferences.fontSize) {
            "tiny" -> theme.applyStyle(R.style.TextSizeTiny, true)
            "small" -> theme.applyStyle(R.style.TextSizeSmall, true)
            "large" -> theme.applyStyle(R.style.TextSizeLarge, true)
            "huge" -> theme.applyStyle(R.style.TextSizeHuge, true)
            "normal",
            null,
            -> theme.applyStyle(R.style.TextSizeNormal, true)
        }
    }

    private fun updateTheme(newTheme: ApplicableStyleUi) {
        val appTheme = when (newTheme) {
            ApplicableStyleUi.Light -> R.style.WykopAppTheme
            ApplicableStyleUi.Dark -> R.style.WykopAppTheme_Dark
            ApplicableStyleUi.DarkAmoled -> R.style.WykopAppTheme_Amoled
        }
        setTheme(appTheme)
    }

    fun showErrorDialog(e: Throwable) {
        if (isRunning) showExceptionDialog(e)
    }

    override fun androidInjector() = androidInjector
}
