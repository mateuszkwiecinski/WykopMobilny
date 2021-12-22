package io.github.wykopmobilny.styles

import io.github.wykopmobilny.ui.base.Query

interface GetAppStyle : Query<StyleUi>

data class StyleUi(
    val theme: AppliedStyleUi,
    val edgeSlidingBehaviorEnabled: Boolean,
)

enum class AppliedStyleUi {
    Light,
    Dark,
    DarkAmoled,
}
