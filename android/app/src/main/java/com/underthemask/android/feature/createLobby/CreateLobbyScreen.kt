package com.underthemask.android.feature.createLobby

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
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.model.HintType
import com.underthemask.android.core.ui.ContentColumn
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.PrimaryAction
import com.underthemask.android.core.ui.SegmentedChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyScreen(
    state: CreateLobbyUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onImpostorCountChange: (Int) -> Unit,
    onHintTypeChange: (HintType) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novi lobby") },
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
                        "Napravi sobu za ekipu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                    OutlinedTextField(
                        value = state.hostName,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillParentMaxWidth(),
                        label = { Text("Ime hosta") },
                        singleLine = true,
                        supportingText = { Text("Najviše 32 karaktera") },
                    )
                    SegmentedChoice(
                        label = "Broj impostora",
                        selected = state.impostorCount,
                        options = listOf(1 to "1", 2 to "2"),
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
                    state.errorMessage?.let { InlineError(it) }
                    PrimaryAction(
                        text = if (state.isSubmitting) "Pravim lobby..." else "Napravi lobby",
                        enabled = state.canSubmit,
                        onClick = onSubmit,
                    )
                }
            }
        }
    }
}
