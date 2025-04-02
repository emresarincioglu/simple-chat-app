package com.example.simplechat.feature.home.view

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.simplechat.core.common.model.Friend
import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.ui.showToast
import com.example.simplechat.feature.home.R
import com.example.simplechat.feature.home.viewmodel.HomeViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.example.simplechat.core.ui.R as coreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showFriendCodeDialog by remember { mutableStateOf(false) }
    var isScanning by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(coreUiR.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = {
                            if (isScanning) return@IconButton
                            isScanning = true

                            val options = GmsBarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .enableAutoZoom()
                                .allowManualInput()
                                .build()
                            val scanner = GmsBarcodeScanning.getClient(context, options)

                            scanner.startScan().addOnCompleteListener {
                                isScanning = false

                                if (it.exception != null) {
                                    context.showToast(
                                        it.exception!!.localizedMessage!!, Toast.LENGTH_SHORT
                                    )
                                } else if (it.isSuccessful) {
                                    val friendCode = it.result.rawValue!!.substringAfterLast('/')
                                    viewModel.addFriend(friendCode)
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_qr_code_scanner),
                            contentDescription = stringResource(R.string.btn_scan_friend_qr_code_cont_desc)
                        )
                    }

                    IconButton(onClick = { showFriendCodeDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.btn_add_friend_cont_desc)
                        )
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
    ) { paddings ->
        val friends by viewModel.friendsStream.collectAsStateWithLifecycle()

        FriendList(
            onNavigateChat = onNavigateChat,
            friends = friends,
            modifier = modifier
                .padding(paddings)
                .fillMaxSize()
        )

        if (showFriendCodeDialog) {
            FriendCodeDialog(viewModel = viewModel, onDismiss = { showFriendCodeDialog = false })
        }
    }
}

@Composable
private fun FriendList(
    onNavigateChat: (Int) -> Unit,
    friends: List<Pair<Friend, Message?>>,
    modifier: Modifier = Modifier
) {
    val placeholder = rememberAsyncImagePainter(model = coreUiR.drawable.avatar_placeholder)
    LazyColumn(modifier = modifier) {
        itemsIndexed(friends, key = { _, (friend, _) -> friend.id }) { index, (friend, message) ->
            Column {
                FriendListItem(onNavigateChat, friend, message, placeholder)

                if (index < friends.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FriendListItem(
    onNavigateChat: (Int) -> Unit,
    friend: Friend,
    lastMessage: Message?,
    placeholder: Painter,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        Text(
            text = friend.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    supportingContent = {
        (lastMessage as? TextMessage)?.let {
            Text(
                text = lastMessage.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    },
    leadingContent = {
        AsyncImage(
            model = friend.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            fallback = placeholder,
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .size(48.dp)
        )
    },
    trailingContent = {
        lastMessage?.let { message ->
            Text(
                text = DateUtils.formatDateTime(
                    LocalContext.current,
                    message.time,
                    DateUtils.FORMAT_SHOW_DATE
                            or DateUtils.FORMAT_ABBREV_MONTH
                            or DateUtils.FORMAT_NO_YEAR
                )
            )
        }
    },
    modifier = modifier
        .clickable { onNavigateChat(friend.id) }
        .fillMaxWidth()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendCodeDialog(
    viewModel: HomeViewModel, onDismiss: () -> Unit, modifier: Modifier = Modifier
) = BasicAlertDialog(onDismissRequest = onDismiss) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 8.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(16.dp)
        ) {
            var showText by rememberSaveable { mutableStateOf(false) }
            val (qr, friendCode) = remember { viewModel.getUserFriendCode() }

            Text(
                text = stringResource(R.string.dialog_share_friend_code_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Image(
                bitmap = qr.asImageBitmap(),
                contentDescription = stringResource(R.string.iv_friend_code_cont_desc),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(250.dp)
                    .padding(top = 16.dp)
                    .clickable { showText = !showText }
            )

            AnimatedVisibility(visible = showText) {
                Text(
                    text = friendCode,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 16.dp)
            ) { Text(stringResource(android.R.string.ok)) }
        }
    }
}