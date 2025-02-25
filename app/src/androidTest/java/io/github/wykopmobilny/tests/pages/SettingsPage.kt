package io.github.wykopmobilny.tests.pages

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.tests.matchers.onPreference
import io.github.wykopmobilny.tests.matchers.tapPreference
import io.github.wykopmobilny.utils.waitVisible
import androidx.preference.R as PreferenceR
import io.github.wykopmobilny.ui.settings.android.R as SettingsR

object SettingsPage {

    private val confirmationOption = withText("Wyłącz potwierdzenie wyjścia z aplikacji")
    private val manageBlacklistOption = withText(SettingsR.string.pref_manage_blacklist)

    fun tapExitConfirmationOption() {
        tapPreference(confirmationOption)
    }

    fun tapAppearance() {
        onView(withText("Ustawienia wyglądu aplikacji")).perform(click())
    }

    fun tapBlacklistSettings() {
        onView(withId(PreferenceR.id.recycler_view))
            .waitVisible()
            .perform(actionOnItem<ViewHolder>(hasDescendant(manageBlacklistOption), click()))
    }

    fun assertConfirmationOptionChecked() {
        onPreference(confirmationOption).waitVisible().check(matches(isChecked()))
    }

    fun assertConfirmationOptionNotChecked() {
        onPreference(confirmationOption).waitVisible().check(matches(isNotChecked()))
    }

    fun assertVisible() {
        onView(withText("Ustawienia")).waitVisible().check(matches(isDisplayed()))
    }
}
