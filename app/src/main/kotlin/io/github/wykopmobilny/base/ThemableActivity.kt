package io.github.wykopmobilny.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.r0adkll.slidr.attachSlidr
import com.r0adkll.slidr.model.SlidrConfig
import io.github.wykopmobilny.styles.ApplicableStyleUi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import io.github.wykopmobilny.ui.base.android.R as BaseR

internal abstract class ThemableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val getAppStyle = requireDependency<StylesDependencies>().getAppStyle()
        val initialStyle = runBlocking { getAppStyle().first() }.style
        updateTheme(initialStyle)
        super.onCreate(savedInstanceState ?: intent.getBundleExtra("saved_State"))

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val slidr = attachSlidr(SlidrConfig(edgeOnly = true))

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
                            slidr.unlock()
                        } else {
                            slidr.lock()
                        }
                    }
            }
        }
    }

    private fun updateTheme(theme: ApplicableStyleUi) {
        val themeRes = when (theme) {
            ApplicableStyleUi.Light -> BaseR.style.Theme_App_Light
            ApplicableStyleUi.Dark -> BaseR.style.Theme_App_Dark
            ApplicableStyleUi.DarkAmoled -> BaseR.style.Theme_App_Amoled
        }
        setTheme(themeRes)
    }
}

internal abstract class ComposableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: intent.getBundleExtra("saved_State"))
        val getAppStyle = requireDependency<StylesDependencies>().getAppStyle()
        setContent {
            val style by getAppStyle().collectAsState(initial = null)
            val colorScheme = style?.style?.colorScheme

            if (colorScheme != null) {
                AppTheme(appTheme = colorScheme) {
                    ScreenContent()
                }
            }
        }
    }

    @Composable
    protected abstract fun ScreenContent()

    private val ApplicableStyleUi.colorScheme
        @Composable get() = when (this) {
            ApplicableStyleUi.Light -> AppTheme.Light
            ApplicableStyleUi.Dark -> AppTheme.Dark
            ApplicableStyleUi.DarkAmoled -> AppTheme.DarkAmoled
        }
}
