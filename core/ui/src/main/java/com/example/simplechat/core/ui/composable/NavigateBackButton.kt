package com.example.simplechat.core.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.simplechat.core.ui.R

@Composable
fun NavigateBackButton(
    onNavigateBack: () -> Unit = { },
    tint: Color = LocalContentColor.current,
    handleSystemBackButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (handleSystemBackButton) {
        BackHandler(onBack = onNavigateBack)
    }

    IconButton(onClick = onNavigateBack, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.btn_nav_back_cont_desc),
            tint = tint
        )
    }
}