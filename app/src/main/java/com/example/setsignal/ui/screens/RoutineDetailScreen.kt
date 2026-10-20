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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import com.example.setsignal.feature.routines.RoutineDetailViewModel
import com.example.setsignal.ui.components.BackHeader
import com.example.setsignal.ui.components.EmptyStateCard
import com.example.setsignal.ui.components.ExerciseRow
import com.example.setsignal.ui.components.MetricRow
import com.example.setsignal.ui.components.Panel
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.components.ValueStepper
import com.example.setsignal.ui.components.signalButtonColors
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun RoutineDetailScreen(
    viewModel: RoutineDetailViewModel,
    onBack: () -> Unit,
    onStart: (restSeconds: Int) -> Unit,
) {
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()
    val routine = viewModel.routine
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
                    title = "Detalle",
                    subtitle = routine?.name,
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (routine == null) {
                item {
                    EmptyStateCard(
                        title = "Rutina no encontrada",
                        message = "Esta rutina ya no está disponible.",
                        accent = MaterialTheme.colorScheme.error,
                    )
                }
                return@ScalingLazyColumn
            }

            item {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmWhite,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Text(
                    text = routine.tagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = Lime,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
            item {
                Panel {
                    MetricRow(label = "Ejercicios", value = routine.exercises.size.toString())
                    Spacer(modifier = Modifier.height(4.dp))
                    MetricRow(
                        label = "Series totales",
                        value = routine.totalSets.toString(),
                        valueColor = Lime,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MetricRow(
                        label = "Descanso sugerido",
                        value = "${routine.recommendedRestSeconds} s",
                    )
                }
            }
            item {
                ValueStepper(
                    label = "Descanso",
                    valueLabel = if (restSeconds == 0) "Sin descanso" else "$restSeconds s",
                    onDecrease = { viewModel.changeRest(-RoutineDetailViewModel.REST_STEP_SECONDS) },
                    onIncrease = { viewModel.changeRest(RoutineDetailViewModel.REST_STEP_SECONDS) },
                    supporting = if (restSeconds == 0) "Pasa directo" else "Pasos de 15 s",
                    decreaseEnabled = restSeconds > 0,
                    increaseEnabled = restSeconds < RoutineDetailViewModel.MAX_ROUTINE_REST_SECONDS,
                )
            }
            item {
                TextButton(
                    onClick = viewModel::useRecommendedRest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Usar sugerido: ${routine.recommendedRestSeconds} s")
                }
            }
            item {
                Button(
                    onClick = { onStart(restSeconds) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = signalButtonColors(),
                ) {
                    Text(
                        text = "EMPEZAR",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
            item {
                SectionLabel(text = "Ejercicios", modifier = Modifier.fillMaxWidth())
            }
            routine.exercises.forEachIndexed { index, exercise ->
                item(key = exercise.id) {
                    ExerciseRow(exercise = exercise, index = index)
                }
            }
            item {
                Text(
                    text = "El reloj cuenta el descanso. Las repeticiones y los tiempos de cada serie los controlas tú.",
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
