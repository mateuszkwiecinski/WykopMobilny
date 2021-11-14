package io.github.wykopmobilny.screenshots

import android.Manifest
import android.util.Log
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.internal.TestNameDetector
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.resources.MaterialAttributes
import com.karumi.shot.ScreenshotTest
import org.junit.Rule

abstract class BaseScreenshotTest : ScreenshotTest {

    @get:Rule
    val mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    abstract fun createFragment(): Fragment

    private val fragmentArgs
        get() = createFragment().arguments

    fun record(
        beforeScreenshot: View.() -> Unit = {},
        size: Size = exactHeight(),
        themes: List<ScreenshotTheme> = ScreenshotTheme.values().toList(),
    ) {
        @Suppress("ThrowingExceptionsWithoutMessageOrCause")
        val testName = Throwable().stackTrace[2].methodName

        themes.forEach { theme ->
            val container = launchFragmentInContainer(
                instantiate = ::createFragment,
                themeResId = theme.theme,
                fragmentArgs = runCatching { fragmentArgs }
                    .onFailure { Log.w("ScreenshotsTests", it) }
                    .getOrNull(),
            )
                .withFragment {
                    beforeScreenshot(requireView())
                    val container = FrameLayout(requireContext()).apply {
                        @Suppress("RestrictedApi")
                        setBackgroundColor(MaterialAttributes.resolveOrThrow(this, android.R.attr.windowBackground))
                    }
                    val parent = requireView().parent as ViewGroup
                    parent.removeView(requireView())
                    container.addView(
                        requireView(),
                        0,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
                    )
                    disableFlakyComponentsAndWaitForIdle(view = container)
                    container.doLayout(size)

                    container
                }

            recordScreenshot(
                name = "$testName[$theme] - $size",
                view = container,
            )
        }
    }

    private fun recordScreenshot(view: View, name: String) {
        val snapshotName = "${TestNameDetector.getTestClass()}_$name"
        Screenshot
            .snap(view)
            .setIncludeAccessibilityInfo(false)
            .setMaxPixels(0)
            .setName(snapshotName)
            .record()
    }

    inline fun <reified T : Any> registerDependencies(dependency: T, scopeId: String? = null) {
        ApplicationProvider.getApplicationContext<ScreenshotTestsApplication>()
            .registerDependency(T::class, scopeId, dependency)
    }

    private fun View.doLayout(deviceSize: Size) {
        disableFlakyComponentsAndWaitForIdle(this)

        if (deviceSize.height <= 0) {
            guessUnboundedHeight(deviceSize.width)
        }
        measure(
            makeMeasureSpec(deviceSize.width, View.MeasureSpec.EXACTLY),
            deviceSize.height
                .takeIf { it > 0 }
                ?.let { makeMeasureSpec(it, View.MeasureSpec.EXACTLY) }
                ?: makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )

        layout(0, 0, measuredWidth, measuredHeight)
    }

    companion object {
        const val avatarUrl = "file:///android_asset/responses/avatar.png"
    }
}

private fun View.guessUnboundedHeight(deviceWidth: Int) {
    guessConstrainedScrollViewHeight()
    guessSwipeRefreshHeight(deviceWidth)
    guessCoordinatorLayoutHeight()
}

private fun View.guessConstrainedScrollViewHeight() {
    allViews
        .filterIsInstance<NestedScrollView>()
        .filter { (it.layoutParams as? ConstraintLayout.LayoutParams)?.height == ConstraintLayout.LayoutParams.MATCH_CONSTRAINT }
        .forEach { it.updateLayoutParams { height = ConstraintLayout.LayoutParams.WRAP_CONTENT } }

    allViews
        .filterIsInstance<NestedScrollView>()
        .filter { ((it.layoutParams as? LinearLayout.LayoutParams)?.weight ?: 0f) > 0 }
        .forEach { it.updateLayoutParams { height = LinearLayout.LayoutParams.WRAP_CONTENT } }
}

private fun View.guessSwipeRefreshHeight(deviceWidth: Int) {
    val swipeRefreshLayouts = allViews
        .filterIsInstance<SwipeRefreshLayout>()
        .toList()
    if (swipeRefreshLayouts.size > 1) {
        error("Max 1 ${SwipeRefreshLayout::class.java.simpleName} supported")
    }

    val swipeRefresh = swipeRefreshLayouts.firstOrNull() ?: return
    val target = swipeRefresh.children.single { it !is ImageView }
    target.measure(
        makeMeasureSpec(deviceWidth, View.MeasureSpec.EXACTLY),
        makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
    )
    swipeRefresh.updateLayoutParams { height = target.measuredHeight }
    swipeRefresh.minimumHeight = target.measuredHeight
}

private fun View.guessCoordinatorLayoutHeight() {
    allViews
        .filterIsInstance<CoordinatorLayout>()
        .flatMap { coordinator ->
            coordinator.children
                .filter { child -> (child.layoutParams as CoordinatorLayout.LayoutParams).behavior is AppBarLayout.ScrollingViewBehavior }
        }
        .forEach { target ->
            val appBarLayout = (target.parent as ViewGroup).children.first { it is AppBarLayout }
            val layoutParams = target.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.behavior = null
            layoutParams.topMargin = appBarLayout.height
            target.layoutParams = layoutParams
        }
    requestLayout()
}

enum class ScreenshotTheme(val theme: Int) {
    Light(R.style.Theme_App_Light),
    Dark(R.style.Theme_App_Dark),
    Amoled(R.style.Theme_App_Amoled)
}

fun exactHeight() = Size(1560, 2880)

fun unboundedHeight() = Size(1560, 0)
