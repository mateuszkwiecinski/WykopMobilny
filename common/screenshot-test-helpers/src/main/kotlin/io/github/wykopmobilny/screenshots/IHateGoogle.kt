package io.github.wykopmobilny.screenshots

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.commitNow
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import io.github.wykopmobilny.screenshots.FragmentScenarioIHateGoogle.EmptyFragmentActivityIHateGoogle.Companion.THEME_EXTRAS_BUNDLE_KEY

inline fun <reified F : Fragment> launchFragmentInContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    crossinline instantiate: () -> F,
): FragmentScenarioIHateGoogle<F> = FragmentScenarioIHateGoogle.launchInContainer(
    F::class.java, fragmentArgs, themeResId, initialState,
    object : FragmentFactory() {
        override fun instantiate(
            classLoader: ClassLoader,
            className: String,
        ) = if (className == F::class.java.name) {
            instantiate()
        } else {
            super.instantiate(classLoader, className)
        }
    },
)

inline fun <reified F : Fragment, T : Any> FragmentScenarioIHateGoogle<F>.withFragment(
    crossinline block: F.() -> T,
): T {
    lateinit var value: T
    var err: Throwable? = null
    onFragment { fragment ->
        runCatching { fragment.block() }
            .onSuccess { value = it }
            .onFailure { err = it }
    }
    err?.let { throw it }
    return value
}

class FragmentScenarioIHateGoogle<F : Fragment>(
    internal val fragmentClass: Class<F>,
    private val activityScenario: ActivityScenario<EmptyFragmentActivityIHateGoogle>,
) {

    fun onFragment(action: FragmentScenario.FragmentAction<F>): FragmentScenarioIHateGoogle<F> {
        activityScenario.onActivity { activity ->
            val fragment = requireNotNull(activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)) {
                "The fragment has been removed from the FragmentManager already."
            }
            check(fragmentClass.isInstance(fragment))
            action.perform(requireNotNull(fragmentClass.cast(fragment)))
        }
        return this
    }

    class EmptyFragmentActivityIHateGoogle : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            setTheme(
                intent.getIntExtra(
                    THEME_EXTRAS_BUNDLE_KEY,
                    R.style.FragmentScenarioEmptyFragmentActivityTheme,
                ),
            )

            // Checks if we have a custom FragmentFactory and set it.
            val factory = FragmentFactoryHolderViewModel.getInstance(this).fragmentFactory
            if (factory != null) {
                supportFragmentManager.fragmentFactory = factory
            }

            // FragmentFactory needs to be set before calling the super.onCreate, otherwise the
            // Activity crashes when it is recreating and there is a fragment which has no
            // default constructor.
            super.onCreate(savedInstanceState)
        }

        companion object {
            const val THEME_EXTRAS_BUNDLE_KEY = "androidx.fragment.app.testing.FragmentScenario" +
                ".EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY"
        }
    }

    internal class FragmentFactoryHolderViewModel : ViewModel() {
        var fragmentFactory: FragmentFactory? = null

        override fun onCleared() {
            super.onCleared()
            fragmentFactory = null
        }

        companion object {
            @Suppress("MemberVisibilityCanBePrivate")
            internal val FACTORY: ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        val viewModel = FragmentFactoryHolderViewModel()
                        return viewModel as T
                    }
                }

            fun getInstance(activity: FragmentActivity): FragmentFactoryHolderViewModel {
                val viewModel: FragmentFactoryHolderViewModel by activity.viewModels { FACTORY }
                return viewModel
            }
        }
    }

    companion object {

        private const val FRAGMENT_TAG = "FragmentScenario_Fragment_Tag"

        fun <F : Fragment> launchInContainer(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle? = null,
            @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
            initialState: Lifecycle.State = Lifecycle.State.RESUMED,
            factory: FragmentFactory? = null,
        ): FragmentScenarioIHateGoogle<F> = internalLaunch(
            fragmentClass,
            fragmentArgs,
            themeResId,
            initialState,
            factory,
        )

        @SuppressLint("RestrictedApi")
        private fun <F : Fragment> internalLaunch(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            @StyleRes themeResId: Int,
            initialState: Lifecycle.State,
            factory: FragmentFactory?,
        ): FragmentScenarioIHateGoogle<F> {
            require(initialState != Lifecycle.State.DESTROYED) {
                "Cannot set initial Lifecycle state to $initialState for FragmentScenario"
            }
            val componentName = ComponentName(
                ApplicationProvider.getApplicationContext(),
                EmptyFragmentActivityIHateGoogle::class.java,
            )
            val startActivityIntent = Intent.makeMainActivity(componentName)
                .putExtra(THEME_EXTRAS_BUNDLE_KEY, themeResId)
            val scenario = FragmentScenarioIHateGoogle(
                fragmentClass = fragmentClass,
                activityScenario = ActivityScenario.launch(
                    startActivityIntent,
                ),
            )

            scenario.activityScenario.onActivity { activity ->
                if (factory != null) {
                    FragmentFactoryHolderViewModel.getInstance(activity).fragmentFactory = factory
                    activity.supportFragmentManager.fragmentFactory = factory
                }
                val fragment = activity.supportFragmentManager.fragmentFactory
                    .instantiate(requireNotNull(fragmentClass.classLoader), fragmentClass.name)
                fragment.arguments = fragmentArgs
                activity.supportFragmentManager.commitNow {
                    add(android.R.id.content, fragment, FRAGMENT_TAG)
                    setMaxLifecycle(fragment, initialState)
                }
            }
            return scenario
        }
    }
}
