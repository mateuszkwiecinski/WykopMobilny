package io.github.wykopmobilny.utils.bindings

import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.isVisible
import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import kotlinx.coroutines.flow.Flow

suspend fun Flow<ProgressButtonUi>.collectProgressInput(button: Button, progress: ProgressBar) {
    collect { buttonUi ->
        when (buttonUi) {
            ProgressButtonUi.Loading -> {
                button.isVisible = false
                progress.isVisible = true
                button.setOnClick(null)
            }
            is ProgressButtonUi.Default -> {
                button.isVisible = true
                progress.isVisible = false
                button.setOnClick(buttonUi.onClicked)
            }
        }
    }
}
