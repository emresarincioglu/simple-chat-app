package com.example.simplechat.feature.authentication.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.ui.composable.CleanableOutlinedTextField
import com.example.simplechat.core.ui.composable.PasswordOutlinedTextField
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.authentication.R
import com.example.simplechat.feature.authentication.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateHome: () -> Unit,
    onNavigateSignup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { result ->
            isLoading = result is Result.Loading
            when (result) {
                is Result.Error -> context.showToast(
                    result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                )

                Result.Success(true) -> onNavigateHome()

                Result.Success(false) -> context.showToast(
                    R.string.toast_login_failed, Toast.LENGTH_SHORT
                )

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
                email = viewModel.email,
                onEmailChanged = { viewModel.email = it },
                password = viewModel.password,
                onPasswordChanged = { viewModel.password = it },
                modifier = Modifier.width(TextFieldDefaults.MinWidth)
            )

            AnimatedVisibility(isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            }

            AuthButtons(
                onLogIn = {
                    if (!isLoading) {
                        viewModel.logIn()
                    }
                },
                onSignUp = {
                    viewModel.cancelRunningActions()
                    onNavigateSignup()
                },
                modifier = Modifier
                    .width(TextFieldDefaults.MinWidth)
                    .padding(top = 24.dp)
            )
        }
    }
}

@Composable
private fun InputTextFields(
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) = Column(modifier = modifier) {
    CleanableOutlinedTextField(
        text = email,
        onTextChange = onEmailChanged,
        label = { Text(stringResource(R.string.tf_email_label)) },
        leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false
        ),
        modifier = Modifier.fillMaxWidth()
    )

    PasswordOutlinedTextField(
        password = password,
        onPasswordChange = onPasswordChanged,
        label = stringResource(R.string.tf_password_label),
        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthButtons(onLogIn: () -> Unit, onSignUp: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(modifier = modifier) {
        Button(
            onClick = {
                keyboardController?.hide()
                onLogIn()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_login_text))
        }

        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            Text(stringResource(R.string.btn_signup_text))
        }
    }
}