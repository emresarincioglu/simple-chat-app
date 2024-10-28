package com.example.simplechat.feature.authentication.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.ui.theme.SimpleChatTheme
import com.example.simplechat.feature.authentication.R
import com.example.simplechat.feature.authentication.viewmodel.PasswordResetViewModel


@Composable
fun PasswordResetScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(false) }

    BackHandler { onPopBackStack(navController, viewModel) }

    LaunchedEffect(Unit) {
        viewModel.sendPasswordResetEmailResult.collect { result ->
            isLoading = result is Result.Loading
            when (result) {
                is Result.Error -> Toast.makeText(
                    context, result.exception.localizedMessage, Toast.LENGTH_SHORT
                ).show()

                Result.Success(true) -> Toast.makeText(
                    context, R.string.toast_email_sent, Toast.LENGTH_SHORT
                ).show()

                Result.Success(false) -> Toast.makeText(
                    context, R.string.toast_email_could_not_send, Toast.LENGTH_SHORT
                ).show()

                else -> Unit
            }
        }
    }

    Scaffold(modifier = modifier) { paddings ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
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
                modifier = Modifier.align(Alignment.Center)
            ) {
                CleanableOutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text(stringResource(R.string.tf_email_label)) },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false
                    )
                )

                AnimatedVisibility(isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

                Button(
                    onClick = {
                        if (!isLoading) {
                            viewModel.sendPasswordResetEmail()
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.btn_password_reset_text))
                }
            }
        }
    }
}

private fun onPopBackStack(navController: NavHostController, viewModel: PasswordResetViewModel) {
    navController.previousBackStackEntry!!.savedStateHandle["email"] = viewModel.email
    navController.popBackStack()
}

@Preview
@Composable
fun PasswordResetScreenPreview() {
    SimpleChatTheme {
        PasswordResetScreen(rememberNavController())
    }
}