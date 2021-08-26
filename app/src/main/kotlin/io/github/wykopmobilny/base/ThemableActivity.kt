package io.github.wykopmobilny.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import io.github.wykopmobilny.R
import io.github.wykopmobilny.styles.AppThemeUi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.utils.requireDependency
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal abstract class ThemableActivity : AppCompatActivity() {

    private val getAppStyle by lazy { requireDependency<StylesDependencies>().getAppStyle() }

    override fun onCreate(savedInstanceState: Bundle?) {
        val initialTheme = runBlocking { getAppStyle().first() }.theme
        updateTheme(initialTheme)
        super.onCreate(savedInstanceState ?: intent.getBundleExtra("saved_State"))

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val slidr = Slidr.attach(this, SlidrConfig(edgeOnly = true))

        lifecycleScope.launchWhenResumed {
            val shared = getAppStyle().stateIn(this)
            launch {
                shared
                    .map { it.theme }
                    .distinctUntilChanged()
                    .dropWhile { it == initialTheme }
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

    private fun updateTheme(theme: AppThemeUi) {
        val themeRes = when (theme) {
            AppThemeUi.Light -> R.style.Theme_App_Light
            AppThemeUi.Dark -> R.style.Theme_App_Dark
            AppThemeUi.DarkAmoled -> R.style.Theme_App_Amoled
        }
        setTheme(themeRes)
    }
}
