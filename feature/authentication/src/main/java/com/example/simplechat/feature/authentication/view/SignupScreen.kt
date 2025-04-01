package com.example.simplechat.feature.authentication.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.ui.theme.SimpleChatTheme
import com.example.simplechat.core.ui.composable.CleanableOutlinedTextField
import com.example.simplechat.core.ui.composable.PasswordOutlinedTextField
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.authentication.R
import com.example.simplechat.feature.authentication.viewmodel.SignupViewModel

@Composable
fun SignupScreen(
    navController: NavHostController,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(false) }

    BackHandler { onPopBackStack(navController, viewModel) }

    LaunchedEffect(Unit) {
        viewModel.signupResult.collect { result ->
            isLoading = result is Result.Loading
            when (result) {
                is Result.Error -> context.showToast(
                    result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                )

                Result.Success(true) -> onNavigateHome()

                Result.Success(false) -> context.showToast(
                    R.string.toast_signup_failed, Toast.LENGTH_SHORT
                )

                else -> Unit
            }
        }
    }

    Scaffold(modifier = modifier) { paddings ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = { onPopBackStack(navController, viewModel) },
                modifier = Modifier
                    .alpha(0.6f)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_nav_back_cont_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                InputTextFields(
                    viewModel = viewModel,
                    modifier = Modifier.width(TextFieldDefaults.MinWidth)
                )

                AnimatedVisibility(visible = isLoading) { CircularProgressIndicator() }

                val keyboardController = LocalSoftwareKeyboardController.current
                Button(
                    onClick = {
                        if (!isLoading) {
                            viewModel.signUp()
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier.width(TextFieldDefaults.MinWidth)
                ) {
                    Text(stringResource(R.string.btn_signup_text))
                }
            }
        }
    }
}

@Composable
private fun InputTextFields(viewModel: SignupViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        CleanableOutlinedTextField(
            text = viewModel.name,
            onTextChange = { viewModel.name = it },
            label = { Text(stringResource(R.string.tf_full_name_label)) },
            leadingIcon = { Icon(Icons.Filled.Person, null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words, autoCorrectEnabled = false
            ),
            modifier = Modifier.fillMaxWidth()
        )

        CleanableOutlinedTextField(
            text = viewModel.email,
            onTextChange = { viewModel.email = it },
            label = { Text(stringResource(R.string.tf_email_label)) },
            leadingIcon = { Icon(Icons.Filled.Email, null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
                autoCorrectEnabled = false
            ),
            modifier = Modifier.fillMaxWidth()
        )

        PasswordOutlinedTextField(
            password = viewModel.password,
            onPasswordChange = { viewModel.password = it },
            label = stringResource(R.string.tf_password_label),
            leadingIcon = { Icon(Icons.Filled.Lock, null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun onPopBackStack(navController: NavHostController, viewModel: SignupViewModel) {
    with(navController.previousBackStackEntry!!) {
        savedStateHandle["email"] = viewModel.email
        savedStateHandle["password"] = viewModel.password
    }

    navController.popBackStack()
}