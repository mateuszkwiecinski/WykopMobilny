package io.github.wykopmobilny.ui.settings.android

import io.github.wykopmobilny.screenshots.BaseScreenshotTest
import io.github.wykopmobilny.ui.settings.AppearancePreferencesUi
import io.github.wykopmobilny.ui.settings.GeneralPreferencesUi
import io.github.wykopmobilny.ui.settings.GetAppearancePreferences
import io.github.wykopmobilny.ui.settings.GetGeneralPreferences
import io.github.wykopmobilny.ui.settings.ListSetting
import io.github.wykopmobilny.ui.settings.Setting
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.ui.settings.SliderSetting
import kotlinx.coroutines.flow.flowOf

internal fun BaseScreenshotTest.registerSettings(
    general: () -> GeneralPreferencesUi = { error("unsupported") },
    appearance: () -> AppearancePreferencesUi = { error("unsupported") },
) = registerDependencies<SettingsDependencies>(
    object : SettingsDependencies {

        override fun general() = object : GetGeneralPreferences {
            override fun invoke() = flowOf(general())
        }

        override fun appearance() = object : GetAppearancePreferences {
            override fun invoke() = flowOf(appearance())
        }
    },
)

internal fun stubSetting(value: Boolean, isEnabled: Boolean = true) = Setting(
    currentValue = value,
    isEnabled = isEnabled,
    onClicked = {},
)

internal fun <T> stubListSetting(value: T, isEnabled: Boolean = true) = ListSetting(
    currentValue = value,
    isEnabled = isEnabled,
    values = emptyList(),
    onSelected = {},
)

internal fun stubSliderSetting(value: Int, range: IntRange, isEnabled: Boolean = true) = SliderSetting(
    currentValue = value,
    isEnabled = isEnabled,
    values = range,
    onChanged = {},
)
