package io.github.wykopmobilny.ui.base.components

sealed class ProgressButtonUi {

    object Loading : ProgressButtonUi()
    data class Default(val onClicked: () -> Unit) : ProgressButtonUi()
}
