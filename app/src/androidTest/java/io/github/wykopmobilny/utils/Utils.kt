package io.github.wykopmobilny.utils

import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.util.HumanReadables
import junit.framework.AssertionFailedError

internal fun ViewInteraction.waitVisible(timeout: Long = 3000L): ViewInteraction {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout

    do {
        try {
            check(matches(isDisplayed()))
            return this
        } catch (e: AssertionFailedError) {
            Thread.sleep(50)
        } catch (e: NoMatchingViewException) {
            Thread.sleep(50)
        }
    } while (System.currentTimeMillis() < endTime)

    return check(matches(isDisplayed()))
}

internal fun ViewInteraction.waitNotVisible(timeout: Long = 3000L): ViewInteraction {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout

    do {
        try {
            check(isNotDisplayed())
            return this
        } catch (e: AssertionFailedError) {
            Thread.sleep(50)
        } catch (e: NoMatchingViewException) {
            Thread.sleep(50)
        }
    } while (System.currentTimeMillis() < endTime)

    return check(isNotDisplayed())
}

fun isNotDisplayed() = ViewAssertion { view, _ ->
    if (isDisplayed().matches(view)) {
        throw AssertionError(
            "View is present in the hierarchy and Displayed: " +
                HumanReadables.describe(view),
        )
    }
}
