package com.underthemask.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.underthemask.android.core.ui.theme.UnderTheMaskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color(0xFF0B0C0E).toArgb()),
        )
        setContent {
            UnderTheMaskTheme {
                LocalNetworkPermissionGate {
                    AppNavHost()
                }
            }
        }
    }
}
