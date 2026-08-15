package com.underthemask.android.feature.createLobby

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.model.HintType
import com.underthemask.android.core.ui.AppBackground
import com.underthemask.android.core.ui.AppPanel
import com.underthemask.android.core.ui.AppTextField
import com.underthemask.android.core.ui.AppTopBar
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.PrimaryAction
import com.underthemask.android.core.ui.SectionHeader
import com.underthemask.android.core.ui.SegmentedChoice

@Composable
fun CreateLobbyScreen(
    state: CreateLobbyUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onImpostorCountChange: (Int) -> Unit,
    onHintTypeChange: (HintType) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    AppBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = { AppTopBar(title = "Novi lobby", eyebrow = "Podešavanje partije", onBack = onBack) },
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
                        SectionHeader(
                            eyebrow = "Korak 01",
                            title = "Napravi sobu za ekipu",
                            trailing = "Ti si host",
                        )
                        Text(
                            "Izaberi osnovna pravila. Podešavanja možeš menjati sve dok partija ne počne.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppPanel {
                            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                AppTextField(
                                    value = state.hostName,
                                    onValueChange = onNameChange,
                                    label = "Ime hosta",
                                    supportingText = "Najviše 32 karaktera",
                                    enabled = !state.isSubmitting,
                                    imeAction = ImeAction.Done,
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onDone = { focusManager.clearFocus() },
                                    ),
                                )
                                SegmentedChoice(
                                    label = "Broj impostora",
                                    selected = state.impostorCount,
                                    options = listOf(1 to "1 impostor", 2 to "2 impostora"),
                                    enabled = !state.isSubmitting,
                                    onSelected = onImpostorCountChange,
                                )
                                SegmentedChoice(
                                    label = "Pomoć za impostora",
                                    selected = state.hintType,
                                    options = listOf(HintType.CATEGORY to "Kategorija", HintType.ASSOCIATION to "Asocijacija"),
                                    enabled = !state.isSubmitting,
                                    onSelected = onHintTypeChange,
                                )
                            }
                        }
                        state.errorMessage?.let { InlineError(it) }
                        PrimaryAction(
                            text = if (state.isSubmitting) "Pravim lobby..." else "Napravi lobby",
                            enabled = state.canSubmit,
                            onClick = {
                                focusManager.clearFocus()
                                onSubmit()
                            },
                        )
                    }
                }
            }
        }
    }
}
