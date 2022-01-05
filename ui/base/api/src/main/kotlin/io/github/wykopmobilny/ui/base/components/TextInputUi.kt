package io.github.wykopmobilny.ui.base.components

data class TextInputUi(
    val text: String,
    val onChanged: (String) -> Unit,
)
