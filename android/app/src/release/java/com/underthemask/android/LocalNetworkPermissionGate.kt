package com.underthemask.android

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable

@Composable
fun ComponentActivity.LocalNetworkPermissionGate(content: @Composable () -> Unit) {
    content()
}
