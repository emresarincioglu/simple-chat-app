package com.example.simplechat.feature.authentication.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.core.common.Result
import com.example.simplechat.feature.authentication.R
import com.example.simplechat.feature.authentication.navigation.NavRoute
import com.example.simplechat.feature.authentication.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { result ->
            isLoading = result is Result.Loading
            when (result) {
                is Result.Error -> Toast.makeText(
                    context, result.exception.localizedMessage, Toast.LENGTH_SHORT
                ).show()

                Result.Success(true) -> {
                    onNavigateHome()
                }

                Result.Success(false) -> Toast.makeText(
                    context, R.string.toast_login_failed, Toast.LENGTH_SHORT
                ).show()

                else -> Unit
            }
        }
    }

    Scaffold(modifier = modifier) { paddings ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
        ) {
            InputTextFields(
                viewModel = viewModel,
                onForgetPassword = { navController.navigate(NavRoute.PasswordResetScreen.route) },
                modifier = Modifier.width(TextFieldDefaults.MinWidth)
            )

            AnimatedVisibility(isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            }

            AuthButtons(
                onLogIn = {
                    if (!isLoading) {
                        viewModel.login()
                    }
                },
                onSignUp = { navController.navigate(NavRoute.SignupScreen.route) },
                modifier = Modifier
                    .width(TextFieldDefaults.MinWidth)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun CleanableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var hasFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = {
            AnimatedVisibility(
                visible = hasFocus && value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.ic_clear_cont_desc)
                    )
                }
            }
        },
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier.onFocusChanged { hasFocus = it.isFocused }
    )
}

@Composable
fun PasswordOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { leadingIcon?.invoke() },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                if (visible) {
                    Icon(
                        painterResource(R.drawable.ic_visibility_24),
                        contentDescription = stringResource(R.string.ic_visibility_cont_desc)
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_visibility_off_24),
                        contentDescription = stringResource(R.string.ic_visibility_off_cont_desc)
                    )
                }
            }
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        modifier = modifier
    )
}

@Composable
fun InputTextFields(
    viewModel: LoginViewModel,
    onForgetPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CleanableOutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text(stringResource(R.string.tf_email_label)) },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        PasswordOutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = stringResource(R.string.tf_password_label),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = onForgetPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.btn_forget_password_text))
        }
    }
}

@Composable
fun AuthButtons(onLogIn: () -> Unit, onSignUp: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Button(onClick = onLogIn, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_login_text))
        }

        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(stringResource(R.string.btn_signup_text))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController(), onNavigateHome = {})
}