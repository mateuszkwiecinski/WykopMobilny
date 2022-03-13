package io.github.wykopmobilny.screenshots

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.cash.paparazzi.Paparazzi
import io.github.wykopmobilny.utils.theme.AppTheme

fun <T> Paparazzi.verifyAppState(
    vararg states: Pair<String, T>,
    content: @Composable ScreenshotDependencies<T>.() -> Unit,
) {
    states.forEach { (scenario, state) ->
        AppTheme.values().forEach { appTheme ->
            snapshot(name = "$scenario[$appTheme]") {
                val navController = rememberNavController()
                val dependencies = object : ScreenshotDependencies<T> {
                    override val navController = navController
                    override val state = state
                }
                AppTheme(appTheme = appTheme) {
                    content(dependencies)
                }
            }
        }
    }
}

interface ScreenshotDependencies<T> {
    val navController: NavController
    val state: T
}
