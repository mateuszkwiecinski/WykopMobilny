package io.github.wykopmobilny.screenshots

import android.Manifest
import android.util.Log
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
import com.facebook.testing.screenshot.TestNameDetector
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.resources.MaterialAttributes
import com.google.android.material.textfield.TextInputLayout
import org.junit.Rule
import io.github.wykopmobilny.ui.base.android.R as BaseR

abstract class BaseScreenshotTest {

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
                    container.disableFlakyComponents()
                    do {
                        container.doLayout(size)
                    } while (container.isLayoutRequested)
                    container.allViews.forEach { it.viewTreeObserver.dispatchOnGlobalLayout() }
                    container.allViews.forEach { it.viewTreeObserver.dispatchOnPreDraw() }

                    container
                }

            recordScreenshot(
                name = "$testName[$theme]-${size.width}x${size.height.takeIf { it > 0 } ?: "Wrap"}",
                view = container,
            )
        }
    }

    private fun View.disableFlakyComponents() {
        allViews.filter {
            it is NestedScrollView ||
                it is HorizontalScrollView ||
                it is ScrollView
        }
            .forEach(::hideViewBars)
        allViews.filterIsInstance<EditText>().forEach { it.isCursorVisible = false }
        allViews.filterIsInstance<TextInputLayout>().forEach { it.isHintAnimationEnabled = false }
    }

    private fun hideViewBars(it: View) {
        it.isHorizontalScrollBarEnabled = false
        it.isVerticalScrollBarEnabled = false
        it.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun recordScreenshot(view: View, name: String) {
        val snapshotName = "${TestNameDetector.getTestMethodInfo()?.className?.substringAfterLast(".")}_$name"
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
        disableFlakyComponents()

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
        const val commentImageUrl = "https://www.wykop.pl/cdn/c3201142/comment_1636958437mADXviOOciVItzVex4z9wm.jpg"
        const val avatarUrl = "https://www.wykop.pl/cdn/c3397992/avatar_def,q150.png"
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
    Light(BaseR.style.Theme_App_Light),
    Dark(BaseR.style.Theme_App_Dark),
    Amoled(BaseR.style.Theme_App_Amoled),
}

fun exactHeight() = Size(1560, 2880)

fun unboundedHeight() = Size(1560, 0)
