package io.github.wykopmobilny.screenshots

import android.Manifest
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
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

    fun record(
        beforeScreenshot: View.() -> Unit = {},
        size: Size = exactHeight(),
        themes: List<ScreenshotTheme> = ScreenshotTheme.values().toList(),
    ) {
        @Suppress("ThrowingExceptionsWithoutMessageOrCause")
        val testName = Throwable().stackTrace[2].methodName

        themes.forEach { theme ->
            val container = launchFragmentInContainer(instantiate = ::createFragment, themeResId = theme.theme)
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
                    container.doLayout(size)

                    container
                }

            compareScreenshot(
                name = "$testName[$theme]",
                view = container,
                widthInPx = container.measuredWidth,
                heightInPx = container.measuredHeight,
            )
        }
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
}

private fun View.guessUnboundedHeight(deviceWidth: Int) {
    guessConstrainedScrollViewHeight()
    guessSwipeRefreshHeight(deviceWidth)
}

private fun View.guessConstrainedScrollViewHeight() {
    allChildren()
        .filterIsInstance<NestedScrollView>()
        .filter { (it.layoutParams as? ConstraintLayout.LayoutParams)?.height == ConstraintLayout.LayoutParams.MATCH_CONSTRAINT }
        .forEach { it.updateLayoutParams { height = ConstraintLayout.LayoutParams.WRAP_CONTENT } }
}

private fun View.guessSwipeRefreshHeight(deviceWidth: Int) {
    val swipeRefreshLayouts = allChildren()
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

fun View.allChildren(): Sequence<View> =
    if (this is ViewGroup) {
        sequenceOf(this) + children.flatMap { it.allChildren() }
    } else {
        sequenceOf(this)
    }

enum class ScreenshotTheme(val theme: Int) {
    Light(R.style.Theme_App_Light),
    Dark(R.style.Theme_App_Dark),
    Amoled(R.style.Theme_App_Amoled)
}

fun exactHeight() = Size(1560, 2880)

fun unboundedHeight() = Size(1560, 0)
