package io.github.wykopmobilny.tests.pages

import androidx.annotation.IdRes
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.google.android.material.navigation.NavigationView
import io.github.wykopmobilny.tests.base.Page
import io.github.wykopmobilny.utils.waitVisible

object MainPage : Page {

    private val drawer = isAssignableFrom(DrawerLayout::class.java)

    private val navigationView = isAssignableFrom(NavigationView::class.java)

    fun tapDrawerOption(@IdRes option: Int) {
        onView(navigationView).waitVisible().perform(navigateTo(option))
    }

    fun openDrawer() {
        onView(drawer).waitVisible().perform(open())
    }

    fun closeDrawer() {
        onView(drawer).waitVisible().perform(close())
    }
}
