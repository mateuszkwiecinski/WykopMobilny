package io.github.wykopmobilny.domain.styles

import io.github.wykopmobilny.domain.settings.prefs.GetAppearanceSectionPreferences
import io.github.wykopmobilny.styles.AppThemeUi
import io.github.wykopmobilny.styles.GetAppStyle
import io.github.wykopmobilny.styles.StyleUi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class GetAppStyleQuery @Inject constructor(
    private val getAppTheme: GetAppTheme,
    private val getAppearanceSectionPreferences: GetAppearanceSectionPreferences,
) : GetAppStyle {

    override fun invoke() =
        combine(
            getAppTheme(),
            getAppearanceSectionPreferences(),
        ) { theme, appearance ->
            StyleUi(
                theme = theme.toUi(),
                edgeSlidingBehaviorEnabled = !appearance.disableEdgeSlide,
            )
        }
            .distinctUntilChanged()
}

private fun AppTheme.toUi() = when (this) {
    AppTheme.Light -> AppThemeUi.Light
    AppTheme.Dark -> AppThemeUi.Dark
    AppTheme.DarkAmoled -> AppThemeUi.DarkAmoled
}
