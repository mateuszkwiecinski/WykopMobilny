package io.github.wykopmobilny.tests.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.R
import io.github.wykopmobilny.utils.waitNotVisible
import io.github.wykopmobilny.utils.waitVisible
import org.hamcrest.CoreMatchers.allOf

object BlacklistPage {

    private val tagsTab = withText("Tagi")
    private val usersTab = withText("Użytkownicy")
    private fun lockIcon(label: String) =
        allOf(
            withId(R.id.btnAction),
            withParent(hasSibling(withText(label))),
        )

    fun tapUsersTab() {
        onView(usersTab).waitVisible().perform(click())
    }

    fun tapTagsTab() {
        onView(tagsTab).waitVisible().perform(click())
    }

    fun tapUnblockTag(tag: String) {
        onView(lockIcon(tag)).waitVisible().perform(click())
    }

    fun tapUnblockUser(user: String) {
        onView(lockIcon(user)).waitVisible().perform(click())
    }

    fun tapImportButton() {
        onView(withText("Zaimportuj")).waitVisible().perform(click())
    }

    fun assertVisible() {
        onView(withText("Zarządzaj czarną listą")).waitVisible()
    }

    fun assertBlockedUserVisible(user: String) {
        onView(usersTab).waitVisible().check(matches(isSelected()))
        onView(withText(user)).waitVisible()
    }

    fun assertBlockedUserNotVisible(user: String) {
        onView(usersTab).waitVisible().check(matches(isSelected()))
        onView(withText(user)).waitNotVisible()
    }

    fun assertBlockedTagVisible(tag: String) {
        onView(tagsTab).waitVisible().check(matches(isSelected()))
        onView(withText(tag)).waitVisible()
    }

    fun assertBlockedTagNotVisible(tag: String) {
        onView(tagsTab).waitVisible().check(matches(isSelected()))
        onView(withText(tag)).waitNotVisible()
    }
}
