package com.example.simplechat.core.ui.composable

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.simplechat.core.ui.R

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    placeholder: String? = null,
    supportingText: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                if (showPassword) {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility_24),
                        contentDescription = stringResource(R.string.ic_visibility_cont_desc)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility_off_24),
                        contentDescription = stringResource(R.string.ic_visibility_off_cont_desc)
                    )
                }
            }
        },
        placeholder = placeholder?.let {
            { Text(it) }
        },
        supportingText = supportingText,
        isError = isError,
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        modifier = modifier.width(TextFieldDefaults.MinWidth)
    )
}

@Composable
fun PasswordOutlinedTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        leadingIcon = { leadingIcon?.invoke() },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                if (visible) {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility_24),
                        contentDescription = stringResource(R.string.ic_visibility_cont_desc)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility_off_24),
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