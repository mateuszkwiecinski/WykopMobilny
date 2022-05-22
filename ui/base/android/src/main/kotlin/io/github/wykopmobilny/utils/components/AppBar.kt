package io.github.wykopmobilny.utils.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import io.github.wykopmobilny.ui.base.android.R

@Composable
fun AppAppBarBackIcon(navController: NavController) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    IconButton(
        onClick = {
            if (!navController.navigateUp()) {
                dispatcher?.onBackPressed()
            }
        },
        content = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.navigation_back),
            )
        },
    )
}
