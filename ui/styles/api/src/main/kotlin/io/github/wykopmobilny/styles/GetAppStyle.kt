package io.github.wykopmobilny.styles

import io.github.wykopmobilny.ui.base.Query

interface GetAppStyle : Query<StyleUi>

data class StyleUi(
    val style: ApplicableStyleUi,
    val edgeSlidingBehaviorEnabled: Boolean,
)

enum class ApplicableStyleUi {
    Light,
    Dark,
    DarkAmoled,
}
