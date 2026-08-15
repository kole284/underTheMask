package com.underthemask.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import com.underthemask.android.core.ui.theme.UnderTheMaskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnderTheMaskTheme(darkTheme = isSystemInDarkTheme()) {
                Surface {
                    LocalNetworkPermissionGate {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
