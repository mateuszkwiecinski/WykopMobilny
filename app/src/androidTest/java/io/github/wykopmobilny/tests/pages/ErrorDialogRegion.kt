package io.github.wykopmobilny.tests.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible

object ErrorDialogRegion : Page {

    fun assertVisible(text: String) {
        onView(withText(text)).waitVisible()
    }

    fun tapButton(text: String = "OK") {
        onView(withText(text)).inRoot(isDialog()).waitVisible().perform(click())
    }
}
