package com.underthemask.android.feature

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.underthemask.android.core.ui.theme.UnderTheMaskTheme
import com.underthemask.android.feature.createLobby.CreateLobbyScreen
import com.underthemask.android.feature.createLobby.CreateLobbyUiState
import com.underthemask.android.feature.joinLobby.JoinLobbyScreen
import com.underthemask.android.feature.joinLobby.JoinLobbyUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FormScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createLobbyFormEnablesValidSubmit() {
        var submitted = false
        composeRule.setContent {
            UnderTheMaskTheme(darkTheme = true) {
                CreateLobbyScreen(
                    state = CreateLobbyUiState(hostName = "Mina"),
                    onBack = {},
                    onNameChange = {},
                    onImpostorCountChange = {},
                    onHintTypeChange = {},
                    onSubmit = { submitted = true },
                )
            }
        }

        composeRule.onNodeWithText("Napravi lobby").assertIsEnabled().performClick()
        assertTrue(submitted)
    }

    @Test
    fun joinLobbyFormEnablesValidSubmit() {
        var submitted = false
        composeRule.setContent {
            UnderTheMaskTheme(darkTheme = true) {
                JoinLobbyScreen(
                    state = JoinLobbyUiState(playerName = "Luka", lobbyCode = "ABC234"),
                    onBack = {},
                    onNameChange = {},
                    onCodeChange = {},
                    onSubmit = { submitted = true },
                )
            }
        }

        composeRule.onNodeWithText("Pridruži se lobbyju").assertIsEnabled().performClick()
        assertTrue(submitted)
    }
}
