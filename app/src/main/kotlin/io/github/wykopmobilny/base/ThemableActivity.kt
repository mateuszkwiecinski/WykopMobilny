package io.github.wykopmobilny.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.r0adkll.slidr.attachSlidr
import com.r0adkll.slidr.model.SlidrConfig
import io.github.wykopmobilny.R
import io.github.wykopmobilny.styles.ApplicableStyleUi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.utils.requireDependency
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
            ApplicableStyleUi.Light -> R.style.Theme_App_Light
            ApplicableStyleUi.Dark -> R.style.Theme_App_Dark
            ApplicableStyleUi.DarkAmoled -> R.style.Theme_App_Amoled
        }
        setTheme(themeRes)
    }
}
