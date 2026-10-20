package com.example.setsignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.setsignal.feature.home.HomeViewModel
import com.example.setsignal.ui.components.BrandHeader
import com.example.setsignal.ui.components.EmptyStateCard
import com.example.setsignal.ui.components.MetricRow
import com.example.setsignal.ui.components.Panel
import com.example.setsignal.ui.components.QuickActionCard
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.components.SessionCard
import com.example.setsignal.ui.components.SetSignalMark
import com.example.setsignal.ui.components.signalButtonColors
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onChooseRoutine: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onOpenSession: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = listState,
    ) { contentPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            autoCentering = null,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    BrandHeader()
                }
            }
            item {
                Text(
                    text = "Entrena con intención. Marca cada serie y deja que el reloj lleve el descanso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            item {
                Button(
                    onClick = onChooseRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = signalButtonColors(),
                ) {
                    Text(
                        text = "ELEGIR RUTINA",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
            item {
                SectionLabel(text = "Última sesión", modifier = Modifier.fillMaxWidth())
            }
            item {
                val lastSession = state.lastSession
                if (lastSession == null) {
                    EmptyStateCard(
                        title = "Sin sesiones todavía",
                        message = "Elige una rutina para empezar. Tu historial aparecerá aquí.",
                    )
                } else {
                    SessionCard(
                        session = lastSession,
                        detailed = true,
                        onClick = { onOpenSession(lastSession.id) },
                    )
                }
            }
            item {
                Panel {
                    SectionLabel(text = "Tu registro local")
                    Spacer(modifier = Modifier.height(7.dp))
                    MetricRow(
                        label = "Sesiones completadas",
                        value = state.completedSessions.toString(),
                        valueColor = Lime,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Los datos viven solo en este reloj.",
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                    )
                }
            }
            item {
                SectionLabel(text = "Explorar", modifier = Modifier.fillMaxWidth())
            }
            item {
                QuickActionCard(
                    label = "Historial",
                    subtitle = "Sesiones completadas y canceladas",
                    onClick = onHistory,
                    icon = { SetSignalMark(height = 22.dp) },
                )
            }
            item {
                QuickActionCard(
                    label = "Ajustes",
                    subtitle = "Descanso, vibración y datos",
                    onClick = onSettings,
                    accent = Lime,
                    icon = { com.example.setsignal.ui.components.TimerGlyph(size = 22.dp) },
                )
            }
            item {
                Text(
                    text = "Las rutinas son ejemplos de configuración, no un plan médico.",
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
