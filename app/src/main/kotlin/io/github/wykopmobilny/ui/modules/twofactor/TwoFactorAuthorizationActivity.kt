package io.github.wykopmobilny.ui.modules.twofactor

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.base.ComposableActivity
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.ui.twofactor.android.TwoFactorMain
import io.github.wykopmobilny.utils.InjectableViewModel
import io.github.wykopmobilny.utils.viewModelWrapperFactory

internal class TwoFactorAuthorizationActivity : ComposableActivity() {

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        Napier.i("Recomposing ScreenContent")
        NavHost(navController = navController, startDestination = "two-factor") {
            twoFactorGraph(navController)
        }
    }

    private fun NavGraphBuilder.twoFactorGraph(navController: NavController) {
        navigation(route = "two-factor", startDestination = "main") {
            composable("main") {
                val parentEntry = remember { navController.getBackStackEntry("two-factor") }
                val wrapper = viewModel<InjectableViewModel<TwoFactorAuthDependencies>>(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelWrapperFactory<TwoFactorAuthDependencies>(),
                )
                TwoFactorMain(
                    navController = navController,
                    dependencies = wrapper.dependency,
                )
            }
        }
    }

    companion object {

        fun createIntent(context: Context) =
            Intent(context, TwoFactorAuthorizationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
