package com.example.simplechat.feature.profile.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.ui.composable.CleanableTextField
import com.example.simplechat.core.ui.composable.PasswordTextField
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.profile.R
import com.example.simplechat.feature.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.simplechat.core.ui.R as coreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var dialogType by remember { mutableStateOf(ProfileScreenDialogType.HIDDEN) }

    ObserveActionResults(
        onLogOut = onLogOut,
        changePasswordResultStream = viewModel.changePasswordResultStream,
        deleteUserResultStream = viewModel.deleteUserResultStream,
        logOutResultStream = viewModel.logOutResultStream
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(coreUiR.string.app_name), maxLines = 1) },
                actions = {
                    var showOverflowMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.btn_menu_cont_desc)
                        )
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_item_delete_account)) },
                            onClick = {
                                showOverflowMenu = false
                                dialogType = ProfileScreenDialogType.CONFIRM
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_item_change_passwd)) },
                            onClick = {
                                showOverflowMenu = false
                                dialogType = ProfileScreenDialogType.CHANGE_PASSWORD
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_item_sign_out)) },
                            onClick = {
                                showOverflowMenu = false
                                viewModel.logOut()
                            }
                        )
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
    ) { paddings ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .padding(paddings)
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val user by viewModel.userStream.collectAsStateWithLifecycle()
            var avatar by rememberSaveable(user) { mutableStateOf<Any?>(user?.avatar) }

            // Dialogs
            when (dialogType) {
                ProfileScreenDialogType.CHANGE_NAME -> InputDialog(
                    title = stringResource(R.string.dialog_change_name_title),
                    onDismiss = { dialogType = ProfileScreenDialogType.HIDDEN },
                    onConfirm = {
                        viewModel.changeUserName(it)
                        dialogType = ProfileScreenDialogType.HIDDEN
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                ProfileScreenDialogType.CHANGE_PASSWORD -> PasswordChangeDialog(
                    onDismiss = { dialogType = ProfileScreenDialogType.HIDDEN },
                    onConfirm = {
                        viewModel.changeUserPassword(it)
                        dialogType = ProfileScreenDialogType.HIDDEN
                    }
                )

                ProfileScreenDialogType.CONFIRM -> ConfirmDeleteDialog(
                    onDismiss = { dialogType = ProfileScreenDialogType.HIDDEN },
                    onConfirm = {
                        viewModel.deleteUser()
                        dialogType = ProfileScreenDialogType.HIDDEN
                    }
                )

                else -> Unit
            }

            val context = LocalContext.current
            PickAvatarButton(
                avatar = avatar,
                onPick = { uri ->
                    if (uri != null && !viewModel.isImageSafe(uri)) {
                        context.showToast(coreUiR.string.error_image_is_nsfw, Toast.LENGTH_SHORT)
                    } else {
                        viewModel.changeUserAvatar(uri)
                        avatar = uri
                    }
                },
                modifier = Modifier.size(200.dp)
            )

            // Name area
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clickable { dialogType = ProfileScreenDialogType.CHANGE_NAME }
                    .fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.name),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = user?.name.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        stringResource(R.string.name_desc),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.7f)
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            // Email area
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Email, contentDescription = null)

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.email),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = user?.email.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.email_desc),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun InputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isError by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    AlertDialog(
        title = { Text(title) },
        onDismissRequest = {
            onDismiss()
            isError = false
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                input = ""
            }) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (input.isBlank()) {
                    isError = true
                } else {
                    onConfirm(input)
                    input = ""
                }
            }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            CleanableTextField(
                text = input,
                onTextChange = {
                    input = it
                    isError = false
                },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            stringResource(R.string.error_blank_input),
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("")
                    }
                },
                keyboardOptions = keyboardOptions
            )
        },
        modifier = modifier
    )
}

@Composable
private fun PasswordChangeDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var errorMessageRes by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            errorMessageRes = null
            password = ""
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (passwordConfirm.isBlank() || password.isBlank()) {
                        errorMessageRes = R.string.error_blank_inputs
                    } else if (password != passwordConfirm) {
                        errorMessageRes = R.string.error_inputs_not_match
                    } else {
                        onConfirm(password)
                        password = ""
                        passwordConfirm = ""
                        errorMessageRes = null
                    }
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                password = ""
            }) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.dialog_change_passwd_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordTextField(
                    password = password,
                    onPasswordChange = {
                        password = it
                        errorMessageRes = null
                    },
                    isError = errorMessageRes != null,
                    placeholder = stringResource(R.string.tf_passwd_placeholder)
                )

                PasswordTextField(
                    password = passwordConfirm,
                    onPasswordChange = {
                        passwordConfirm = it
                        errorMessageRes = null
                    },
                    isError = errorMessageRes != null,
                    placeholder = stringResource(R.string.tf_confirm_passwd_placeholder)
                )

                Text(
                    text = errorMessageRes?.let { stringResource(it) } ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit, modifier: Modifier = Modifier
) = AlertDialog(
    onDismissRequest = onDismiss,
    dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
    },
    confirmButton = {
        TextButton(onClick = onConfirm) { Text(stringResource(android.R.string.ok)) }
    },
    title = { Text(stringResource(R.string.dialog_confirm_delete_user_title)) },
    text = { Text(stringResource(R.string.dialog_confirm_delete_user_text)) },
    modifier = modifier
)

@Composable
private fun PickAvatarButton(avatar: Any?, onPick: (Uri?) -> Unit, modifier: Modifier = Modifier) {
    val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia(), onPick)

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceBright,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
        modifier = modifier
    ) {
        val sizeResolver = rememberConstraintsSizeResolver()
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(avatar)
                .size(sizeResolver)
                .build()
        )

        val loadState by painter.state.collectAsStateWithLifecycle()
        when (loadState) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Error -> Image(
                painter = painterResource(coreUiR.drawable.ic_person_24),
                contentDescription = stringResource(R.string.btn_pick_photo_cont_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .then(sizeResolver)
                    .clip(CircleShape)
                    .fillMaxSize()
                    .padding(16.dp)
            )

            is AsyncImagePainter.State.Loading -> CircularProgressIndicator(modifier = Modifier.fillMaxSize())

            is AsyncImagePainter.State.Success -> Image(
                painter = painter,
                contentDescription = stringResource(R.string.btn_pick_photo_cont_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .then(sizeResolver)
                    .clip(CircleShape)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun ObserveActionResults(
    onLogOut: () -> Unit,
    changePasswordResultStream: Flow<Result<Boolean>>,
    deleteUserResultStream: Flow<Result<Boolean>>,
    logOutResultStream: Flow<Result<Boolean>>
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        launch {
            changePasswordResultStream.collect { result ->
                when (result) {
                    is Result.Error -> context.showToast(
                        result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                    )

                    Result.Success(true) -> context.showToast(
                        R.string.toast_passwd_changed, Toast.LENGTH_SHORT
                    )

                    Result.Success(false) -> context.showToast(
                        R.string.toast_passwd_change_failed, Toast.LENGTH_SHORT
                    )

                    else -> Unit
                }
            }
        }

        launch {
            deleteUserResultStream.collect { result ->
                when (result) {
                    is Result.Error -> context.showToast(
                        result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                    )

                    Result.Success(true) -> onLogOut()

                    Result.Success(false) -> context.showToast(
                        R.string.toast_account_delete_failed, Toast.LENGTH_SHORT
                    )

                    else -> Unit
                }
            }
        }

        logOutResultStream.collect { result ->
            when (result) {
                is Result.Error -> context.showToast(
                    result.exception.localizedMessage!!, Toast.LENGTH_SHORT
                )

                Result.Success(true) -> onLogOut()

                Result.Success(false) -> context.showToast(
                    R.string.toast_logout_failed, Toast.LENGTH_SHORT
                )

                else -> Unit
            }
        }
    }
}

private enum class ProfileScreenDialogType {
    HIDDEN,
    CHANGE_NAME,
    CHANGE_PASSWORD,
    CONFIRM
}