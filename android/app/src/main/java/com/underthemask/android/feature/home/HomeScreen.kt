package com.underthemask.android.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.ui.AppBackground
import com.underthemask.android.core.ui.AppPanel
import com.underthemask.android.core.ui.BrandLockup
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.PrimaryAction
import com.underthemask.android.core.ui.SecondaryAction
import com.underthemask.android.core.ui.StatusPill
import com.underthemask.android.core.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    debugBackendAddress: String?,
    startupMessage: String?,
    onCreateLobby: () -> Unit,
    onJoinLobby: () -> Unit,
) {
    AppBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xLarge),
            ) {
                BrandLockup()

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ko se krije\nispod maske?",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Jedna tajna reč. Nekoliko pažljivih tragova. I neko za stolom ko samo glumi da zna odgovor.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    listOf("Saznaj ulogu", "Daj trag", "Otkrij impostora").forEach { hint ->
                        StatusPill(hint)
                    }
                }

                startupMessage?.let { InlineError(it) }

                AppPanel(highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                "NOVA PARTIJA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text("Okupi ekipu za stolom", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Napravi privatni lobby ili uđi kodom koji si dobio od hosta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        PrimaryAction(text = "Napravi lobby", enabled = true, onClick = onCreateLobby)
                        SecondaryAction(text = "Pridruži se kodom", onClick = onJoinLobby)
                        Text(
                            "3-12 igrača  •  Bez registracije",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (debugBackendAddress != null) {
                    Text(
                        debugBackendAddress,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}
