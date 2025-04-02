package com.example.simplechat.feature.authentication.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.ui.composable.CleanableOutlinedTextField
import com.example.simplechat.core.ui.composable.NavigateBackButton
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.authentication.R
import com.example.simplechat.feature.authentication.viewmodel.PasswordResetViewModel


@Composable
fun PasswordResetScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sendPasswordResetEmailResult.collect { result ->
            isLoading = result is Result.Loading
            when (result) {
                is Result.Error -> context.showToast(
                    result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                )

                Result.Success(true) -> context.showToast(
                    R.string.toast_email_sent, Toast.LENGTH_SHORT
                )

                Result.Success(false) -> context.showToast(
                    R.string.toast_email_could_not_send, Toast.LENGTH_SHORT
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
        ) {
            NavigateBackButton(
                onNavigateBack = onNavigateBack,
                handleSystemBackButton = true,
                tint = MaterialTheme.colorScheme.onBackground
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                CleanableOutlinedTextField(
                    text = viewModel.email,
                    onTextChange = { viewModel.email = it },
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