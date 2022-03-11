package io.github.wykopmobilny.ui.twofactor.android

import android.view.View
import app.cash.paparazzi.Paparazzi
import io.github.wykopmobilny.ui.two_factor.android.R
import org.junit.Rule
import org.junit.Test

class SampleClass {
    @get:Rule
    val paparazzi = Paparazzi(theme = "Base.Theme.App.Light")

    @Test
    fun simple() {
        val view = paparazzi.inflate<View>(R.layout.fragment_two_factor)

        paparazzi.snapshot(view)
    }
}
