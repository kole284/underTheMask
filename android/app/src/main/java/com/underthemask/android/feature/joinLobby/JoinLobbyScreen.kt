package com.underthemask.android.feature.joinLobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthemask.android.core.ui.AppBackground
import com.underthemask.android.core.ui.AppPanel
import com.underthemask.android.core.ui.AppTextField
import com.underthemask.android.core.ui.AppTopBar
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.PrimaryAction
import com.underthemask.android.core.ui.SectionHeader

@Composable
fun JoinLobbyScreen(
    state: JoinLobbyUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        if (state.canSubmit) onSubmit()
    }
    AppBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = { AppTopBar(title = "Pridruživanje", eyebrow = "Privatna partija", onBack = onBack) },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SectionHeader(eyebrow = "Pozivnica", title = "Uđi u lobby")
                        Text(
                            "Unesi šestocifreni kod koji si dobio od hosta i ime po kom će te ekipa prepoznati.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppPanel {
                            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                AppTextField(
                                    value = state.lobbyCode,
                                    onValueChange = onCodeChange,
                                    label = "Lobby kod",
                                    supportingText = "6 slova ili brojeva",
                                    enabled = !state.isSubmitting,
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Next,
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        letterSpacing = 3.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                                AppTextField(
                                    value = state.playerName,
                                    onValueChange = onNameChange,
                                    label = "Ime igrača",
                                    enabled = !state.isSubmitting,
                                    imeAction = ImeAction.Done,
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { submit() }),
                                )
                            }
                        }
                        state.errorMessage?.let { InlineError(it) }
                        PrimaryAction(
                            text = if (state.isSubmitting) "Ulazim..." else "Pridruži se lobbyju",
                            enabled = state.canSubmit,
                            onClick = submit,
                        )
                    }
                }
            }
        }
    }
}
