package com.underthemask.android.feature.joinLobby

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.ui.ContentColumn
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.PrimaryAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinLobbyScreen(
    state: JoinLobbyUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridruzivanje") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Nazad") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        ) {
            item {
                ContentColumn {
                    Text(
                        "Unesi kod i ime igraca",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                    OutlinedTextField(
                        value = state.playerName,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillParentMaxWidth(),
                        label = { Text("Ime igraca") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.lobbyCode,
                        onValueChange = onCodeChange,
                        modifier = Modifier.fillParentMaxWidth(),
                        label = { Text("Lobby kod") },
                        supportingText = { Text("Kod ima 6 slova ili brojeva") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        singleLine = true,
                    )
                    state.errorMessage?.let { InlineError(it) }
                    PrimaryAction(
                        text = if (state.isSubmitting) "Ulazim..." else "Pridruzi se lobbyju",
                        enabled = state.canSubmit,
                        onClick = onSubmit,
                    )
                }
            }
        }
    }
}
