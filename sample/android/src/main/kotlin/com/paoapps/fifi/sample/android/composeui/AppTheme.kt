package com.paoapps.fifi.sample.android.composeui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = {
            Surface(content = content)
        }
    )
}