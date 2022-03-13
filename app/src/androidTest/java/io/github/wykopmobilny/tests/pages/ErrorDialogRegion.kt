package io.github.wykopmobilny.tests.pages

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible

class ErrorDialogRegion(val composeTestRule: ComposeTestRule) : Page {

    fun assertVisible(text: String, interop: Boolean = false) {
        if (interop) {
            onView(withText(text)).waitVisible()
        } else {
            composeTestRule.onNodeWithText(text).assertExists()
        }
    }

    fun tapButton(text: String = "OK", interop: Boolean = false) {
        if (interop) {
            onView(withText(text)).inRoot(isDialog()).waitVisible().perform(click())
        } else {
            composeTestRule.onNodeWithText(text).assertExists()
        }
    }
}
