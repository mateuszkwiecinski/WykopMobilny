package io.github.wykopmobilny.ui.settings.android

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceFragmentCompat
import io.github.wykopmobilny.ui.settings.GeneralPreferencesUi.NotificationsUi.RefreshPeriodUi
import io.github.wykopmobilny.ui.settings.GetGeneralPreferences
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.utils.requireDependency
import kotlinx.coroutines.launch

internal class GeneralPreferencesFragment : PreferenceFragmentCompat() {

    lateinit var getGeneralPreferences: GetGeneralPreferences

    override fun onAttach(context: Context) {
        getGeneralPreferences = context.requireDependency<SettingsDependencies>().general()
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_preferences, rootKey)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                getGeneralPreferences().collect {
                    bindPreference("appearance", ::openAppearanceSettings)
                    bindCheckbox("showNotifications", it.notifications.notificationsEnabled)
                    bindCheckbox("disableExitConfirmation", it.notifications.exitConfirmation)
                    bindCheckbox("showAdultContent", it.filtering.showPlus18Content)
                    bindCheckbox("hideNsfw", it.filtering.hideNsfwContent)
                    bindCheckbox("hideLowRangeAuthors", it.filtering.hideNewUserContent)
                    bindCheckbox("hideContentWithoutTags", it.filtering.hideContentWithNoTags)
                    bindCheckbox("hideBlacklistedViews", it.filtering.hideBlacklistedContent)
                    bindPreference("blacklist", it.filtering.manageBlackList)
                    bindCheckbox("useBuiltInBrowser", it.filtering.useEmbeddedBrowser)
                    bindPreference("clearhistory", it.filtering.clearSearchHistory)
                    bindList(
                        key = "notificationsSchedulerDelay",
                        setting = it.notifications.notificationRefreshPeriod,
                        mapping = refreshPeriodMapping,
                    )
                }
            }
        }
    }

    private val refreshPeriodMapping by lazy {
        RefreshPeriodUi.entries.associateWith { period ->
            when (period) {
                RefreshPeriodUi.FifteenMinutes -> R.string.preferences_notification_period_15_minutes
                RefreshPeriodUi.ThirtyMinutes -> R.string.preferences_notification_period_30_minutes
                RefreshPeriodUi.OneHour -> R.string.preferences_notification_period_1_hour
                RefreshPeriodUi.TwoHours -> R.string.preferences_notification_period_2_hours
                RefreshPeriodUi.FourHours -> R.string.preferences_notification_period_4_hours
                RefreshPeriodUi.EightHours -> R.string.preferences_notification_period_8_hours
            }
                .let { resources.getString(it) }
        }
    }

    private fun openAppearanceSettings() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AppearancePreferencesFragment())
            .addToBackStack(null)
            .commit()
    }
}
