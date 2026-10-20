package com.example.setsignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.setsignal.domain.model.CompletedExercise
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.feature.summary.SummaryUiState
import com.example.setsignal.feature.summary.SummaryViewModel
import com.example.setsignal.ui.components.BackHeader
import com.example.setsignal.ui.components.EmptyStateCard
import com.example.setsignal.ui.components.LinearProgress
import com.example.setsignal.ui.components.MetricRow
import com.example.setsignal.ui.components.Panel
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.components.StatusPill
import com.example.setsignal.ui.components.signalButtonColors
import com.example.setsignal.ui.formatSessionDate
import com.example.setsignal.ui.formatElapsed
import com.example.setsignal.ui.theme.Coral
import com.example.setsignal.ui.theme.Graphite
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onBack: () -> Unit,
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
                BackHeader(
                    title = "Resumen",
                    subtitle = "Lo que realmente hiciste",
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            when (val uiState = state) {
                SummaryUiState.Loading -> item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 28.dp),
                        colors = ProgressIndicatorDefaults.colors(indicatorColor = Lime),
                    )
                }

                SummaryUiState.NotFound -> item {
                    EmptyStateCard(
                        title = "Sesión no encontrada",
                        message = "Puede que el historial se haya borrado.",
                        accent = Coral,
                    )
                }

                is SummaryUiState.Content -> {
                    val session = uiState.session
                    item {
                        StatusPill(
                            text = if (session.wasCompleted) "Completada" else "Cancelada",
                            contentColor = if (session.wasCompleted) Lime else Coral,
                            containerColor = if (session.wasCompleted) {
                                Lime.copy(alpha = 0.12f)
                            } else {
                                Coral.copy(alpha = 0.12f)
                            },
                        )
                    }
                    item {
                        Text(
                            text = session.routineName,
                            style = MaterialTheme.typography.titleLarge,
                            color = WarmWhite,
                            textAlign = TextAlign.Center,
                        )
                    }
                    item {
                        Text(
                            text = formatSessionDate(session.finishedAtEpochMillis),
                            style = MaterialTheme.typography.bodyExtraSmall,
                            color = Mist,
                            textAlign = TextAlign.Center,
                        )
                    }
                    item {
                        Panel {
                            MetricRow(
                                label = "Tiempo activo",
                                value = formatElapsed(session.durationSeconds),
                                valueColor = Lime,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            MetricRow(
                                label = "Series",
                                value = "${session.completedSets}/${session.plannedSets}",
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            MetricRow(
                                label = "Ejercicios",
                                value = "${session.completedExercises}/${session.plannedExercises}",
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            MetricRow(
                                label = "Descanso usado",
                                value = if (session.restSeconds == 0) {
                                    "Sin descanso"
                                } else {
                                    "${session.restSeconds} s"
                                },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgress(
                                progress = session.setProgress,
                                color = if (session.wasCompleted) Lime else Coral,
                            )
                        }
                    }
                    item {
                        SectionLabel(
                            text = "Detalle por ejercicio",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    session.exercises.forEach { exercise ->
                        item(key = exercise.exerciseId) {
                            ExerciseResultRow(exercise)
                        }
                    }
                    item {
                        Text(
                            text = if (session.wasCompleted) {
                                "Sesión completada. El tiempo activo excluye las pausas."
                            } else {
                                "Sesión cancelada. Se guardaron las series completadas hasta ese momento."
                            },
                            style = MaterialTheme.typography.bodyExtraSmall,
                            color = Mist,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = signalButtonColors(),
                ) {
                    Text(
                        text = "VOLVER",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseResultRow(exercise: CompletedExercise) {
    val accent = if (exercise.completedSets > 0) Lime else Mist
    Panel(containerColor = Graphite) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodySmall,
                color = WarmWhite,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                text = "${exercise.completedSets}/${exercise.plannedSets}",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Objetivo: ${exercise.targetLabel}",
            style = MaterialTheme.typography.bodyExtraSmall,
            color = Mist,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgress(
            progress = if (exercise.plannedSets == 0) {
                0f
            } else {
                exercise.completedSets.toFloat() / exercise.plannedSets
            },
            color = accent,
        )
    }
}
