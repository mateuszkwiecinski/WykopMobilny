package io.github.wykopmobilny.ui.twofactor.android

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.BackgroundOpacity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.components.ProgressButtonUi
import io.github.wykopmobilny.ui.base.components.TextInputUi
import io.github.wykopmobilny.ui.two_factor.android.R
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDetailsUi
import io.github.wykopmobilny.utils.bindings.AppErrorDialog
import io.github.wykopmobilny.utils.components.AppAppBarBackIcon
import io.github.wykopmobilny.utils.theme.AppTheme

@Preview
@Composable
fun TestingDefault() {
    val navController = rememberNavController()
    AppTheme(appTheme = AppTheme.DarkAmoled) {
        TwoFactorScaffold(
            navController = navController,
            TwoFactorAuthDetailsUi(
                code = TextInputUi(
                    text = "",
                    onChanged = {},
                ),
                verifyButton = ProgressButtonUi.Loading,
                authenticatorButton = null,
                errorDialog = ErrorDialogUi(
                    error = IllegalStateException("Sorry"),
                    retryAction = {},
                    dismissAction = {},
                ),
            ),
        )
    }
}

@Composable
fun TwoFactorMain(navController: NavController, dependencies: TwoFactorAuthDependencies) {
    val state by dependencies.getTwoFactorAuthDetails().invoke().collectAsState(initial = null)
    TwoFactorScaffold(navController = navController, state = state)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TwoFactorScaffold(navController: NavController, state: TwoFactorAuthDetailsUi?) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(stringResource(id = R.string.two_factor_auth_toolbar))
                },
                navigationIcon = {
                    AppAppBarBackIcon(navController = navController)
                },
            )
        },
        content = { innerPadding ->
            if (state != null) {
                LoadedContent(innerPadding, state)
            }
        },
    )
}

@Composable
private fun LoadedContent(innerPadding: PaddingValues, state: TwoFactorAuthDetailsUi) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .wrapContentSize(align = Alignment.TopCenter)
            .scrollable(
                state = scrollState,
                orientation = Orientation.Vertical,
            )
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.two_factor_auth_message),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.code.text,
            onValueChange = { text -> state.code.onChanged(text) },
            label = { Text(stringResource(id = R.string.two_factor_auth_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.textFieldColors(
                textColor = LocalContentColor.current.copy(LocalContentAlpha.current),
                backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BackgroundOpacity),
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity),
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                errorLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                trailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                errorTrailingIconColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                errorLabelColor = MaterialTheme.colorScheme.error,
                placeholderColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (val verifyButton = state.verifyButton) {
            is ProgressButtonUi.Default ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = verifyButton.onClicked,
                    content = { Text(verifyButton.label) },
                )
            ProgressButtonUi.Loading ->
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(48.dp),
        )
        when (val authButton = state.authenticatorButton) {
            is ProgressButtonUi.Default ->
                OutlinedButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = authButton.onClicked,
                    content = {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = authButton.label,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(authButton.label)
                    },
                )
            ProgressButtonUi.Loading -> Unit // Unsupported in this layout
            null -> Unit
        }

        val errorDialog = state.errorDialog
        if (errorDialog != null) {
            AppErrorDialog(errorDialog)
        }
    }
}
