package io.github.wykopmobilny.ui.base.components

data class OptionPickerUi(
    val title: String,
    val reasons: List<Option>,
    val dismissAction: () -> Unit,
) {

    data class Option(
        val label: String,
        val icon: Drawable? = null,
        val clickAction: () -> Unit,
    )
}
