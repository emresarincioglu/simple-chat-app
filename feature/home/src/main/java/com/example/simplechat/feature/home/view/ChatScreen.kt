package com.example.simplechat.feature.home.view

import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.simplechat.core.common.model.message.ImageMessage
import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.ui.composable.NavigateBackButton
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.home.R
import com.example.simplechat.feature.home.viewmodel.ChatViewModel
import com.example.simplechat.core.ui.R as coreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val friend by viewModel.friendStream.collectAsStateWithLifecycle()
                    LaunchedEffect(friend) {
                        if (friend == null) {
                            onNavigateBack()
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TODO: Remove async painter
                        val painter = rememberAsyncImagePainter(
                            model = coreUiR.drawable.avatar_placeholder
                        )
                        AsyncImage(
                            model = friend?.avatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = painter,
                            fallback = painter,
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                                .size(48.dp)
                        )

                        Text(
                            text = friend?.name.orEmpty(),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    NavigateBackButton(
                        onNavigateBack = onNavigateBack,
                        handleSystemBackButton = true
                    )
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.btn_delete_friend_cont_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
    ) { paddings ->
        val oldMessages = viewModel.oldMessagesStream.collectAsLazyPagingItems()

        Column(modifier = modifier.padding(paddings)) {
            MessageList(
                oldMessages = oldMessages, viewModel = viewModel, modifier = Modifier.weight(1f)
            )

            // TODO: Change background color
            MessageBottomBar(
                viewModel = viewModel,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (showDeleteDialog) {
            ConfirmDeleteDialog(
                onConfirm = {
                    viewModel.deleteFriend()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun MessageList(
    oldMessages: LazyPagingItems<Message>, viewModel: ChatViewModel, modifier: Modifier = Modifier
) {
    val newMessages by viewModel.newMessagesStream.collectAsStateWithLifecycle(emptyList())
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
        reverseLayout = true,
        modifier = modifier
    ) {
        items(newMessages) { message ->
            Spacer(modifier = Modifier.height(8.dp))
            if (message.isFromUser) {
                UserMessageBubble(message)
            } else {
                FriendMessageBubble(message)
            }
        }

        items(count = oldMessages.itemCount) { index ->
            Spacer(modifier = Modifier.height(8.dp))
            val message = oldMessages[index]!!
            if (message.isFromUser) {
                UserMessageBubble(message)
            } else {
                FriendMessageBubble(message)
            }
        }

        if (oldMessages.loadState.append == LoadState.Loading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: Message, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.large.copy(topStart = ZeroCornerSize),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth(fraction = 0.7f)
            .wrapContentWidth(Alignment.Start)
            .clickable { expanded = !expanded }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .width(IntrinsicSize.Min)
            ) {
                Text(
                    text = DateUtils.formatDateTime(
                        LocalContext.current,
                        message.time,
                        DateUtils.FORMAT_SHOW_TIME
                                or DateUtils.FORMAT_SHOW_DATE
                                or DateUtils.FORMAT_ABBREV_ALL
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                    maxLines = 1,
                    modifier = Modifier
                        .alpha(0.8f)
                        .width(IntrinsicSize.Max)
                )

                HorizontalDivider()
            }

            when (message) {
                is TextMessage -> Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                is ImageMessage -> AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp)
                )

                else -> throw Exception("Unhandled message type")
            }
        }
    }
}

@Composable
private fun FriendMessageBubble(message: Message, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.CenterEnd, modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = MaterialTheme.shapes.large.copy(topEnd = ZeroCornerSize),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth(fraction = 0.7f)
                .wrapContentWidth(Alignment.End)
                .clickable { expanded = !expanded }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(IntrinsicSize.Min)
                        .align(Alignment.End)
                ) {
                    Text(
                        text = DateUtils.formatDateTime(
                            LocalContext.current,
                            message.time,
                            DateUtils.FORMAT_SHOW_TIME
                                    or DateUtils.FORMAT_SHOW_DATE
                                    or DateUtils.FORMAT_ABBREV_ALL
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        maxLines = 1,
                        modifier = Modifier
                            .alpha(0.8f)
                            .width(IntrinsicSize.Max)
                    )

                    HorizontalDivider()
                }

                when (message) {
                    is TextMessage -> Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.End)
                    )

                    is ImageMessage -> AsyncImage(
                        model = message.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp)
                    )

                    else -> throw Exception("Unhandled message type")
                }
            }
        }
    }
}

@Composable
private fun MessageBottomBar(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    var message by rememberSaveable { mutableStateOf("") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text(stringResource(R.string.tf_message_placeholder)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true
            ),
            trailingIcon = {
                val context = LocalContext.current
                val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
                    uri?.let {
                        if (viewModel.isImageSafe(uri)) {
                            viewModel.sendImage(uri)
                        } else {
                            context.showToast(
                                coreUiR.string.error_image_is_nsfw, Toast.LENGTH_SHORT
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image_24),
                        contentDescription = stringResource(R.string.btn_send_image_message_cont_desc)
                    )
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                if (message.isNotBlank()) {
                    viewModel.sendMessage(message.trim())
                    message = ""
                }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.btn_send_message_cont_desc),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
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
    title = { Text(stringResource(R.string.dialog_confirm_delete_friend_title)) },
    text = { Text(stringResource(R.string.dialog_confirm_delete_friend_text)) },
    modifier = modifier
)
