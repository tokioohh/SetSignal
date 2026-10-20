package com.example.setsignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.setsignal.feature.history.HistoryViewModel
import com.example.setsignal.ui.components.BackHeader
import com.example.setsignal.ui.components.EmptyStateCard
import com.example.setsignal.ui.components.MetricRow
import com.example.setsignal.ui.components.Panel
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.components.SessionCard
import com.example.setsignal.ui.formatElapsed
import com.example.setsignal.ui.formatHistoryDay
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onOpenSession: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            autoCentering = null,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                BackHeader(
                    title = "Historial",
                    subtitle = "${state.sessions.size} sesiones guardadas",
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 24.dp),
                        colors = ProgressIndicatorDefaults.colors(indicatorColor = Lime),
                    )
                }
                return@ScalingLazyColumn
            }

            if (state.sessions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Aún no hay sesiones",
                        message = "Cuando termines o canceles una rutina, quedará registrada aquí.",
                    )
                }
                item {
                    Text(
                        text = "El historial guarda lo que realmente completaste, no lo que estaba planeado.",
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                        textAlign = TextAlign.Center,
                    )
                }
                return@ScalingLazyColumn
            }

            val completed = state.sessions.count { it.wasCompleted }
            val cancelled = state.sessions.size - completed
            val totalSeconds = state.sessions.sumOf { it.durationSeconds }

            item {
                Panel {
                    SectionLabel(text = "Resumen")
                    Spacer(modifier = Modifier.height(7.dp))
                    MetricRow(
                        label = "Completadas",
                        value = completed.toString(),
                        valueColor = Lime,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MetricRow(
                        label = "Canceladas",
                        value = cancelled.toString(),
                        valueColor = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MetricRow(label = "Tiempo registrado", value = formatElapsed(totalSeconds))
                }
            }

            var previousDay: String? = null
            state.sessions.forEach { session ->
                val day = formatHistoryDay(session.startedAtEpochMillis)
                if (day != previousDay) {
                    item(key = "day-${session.id}") {
                        SectionLabel(
                            text = day,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        )
                    }
                    previousDay = day
                }
                item(key = session.id) {
                    SessionCard(
                        session = session,
                        detailed = true,
                        onClick = { onOpenSession(session.id) },
                    )
                }
            }

            item {
                Text(
                    text = "Tiempo activo, sin contar pausas.",
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
