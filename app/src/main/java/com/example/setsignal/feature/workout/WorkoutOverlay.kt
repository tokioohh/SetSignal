package com.example.setsignal.feature.workout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.example.setsignal.domain.workout.WorkoutPhase
import com.example.setsignal.ui.components.CloseGlyph
import com.example.setsignal.ui.components.ConfirmDialog
import com.example.setsignal.ui.components.PauseGlyph
import com.example.setsignal.ui.components.PlayGlyph
import com.example.setsignal.ui.components.ProgressRing
import com.example.setsignal.ui.components.SetSignalMark
import com.example.setsignal.ui.components.dangerButtonColors
import com.example.setsignal.ui.components.secondaryButtonColors
import com.example.setsignal.ui.components.signalButtonColors
import com.example.setsignal.ui.formatElapsed
import com.example.setsignal.ui.theme.Coral
import com.example.setsignal.ui.theme.Ember
import com.example.setsignal.ui.theme.GraphiteHigh
import com.example.setsignal.ui.theme.Ink
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.OutlineSubtle
import com.example.setsignal.ui.theme.WarmWhite

/**
 * Full-screen live workout surface. It is deliberately outside the navigation graph: an active
 * session cannot be lost with a swipe-back gesture.
 */
@Composable
fun WorkoutOverlay(
    state: WorkoutUiState,
    onCompleteSet: () -> Unit,
    onTogglePause: () -> Unit,
    onAddRest: () -> Unit,
    onSkipRest: () -> Unit,
    onCancel: () -> Unit,
    onRetrySave: () -> Unit,
    onDiscard: () -> Unit,
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val isTerminal = state.phase == WorkoutPhase.FINISHED ||
        state.phase == WorkoutPhase.CANCELLED

    BackHandler {
        if (!state.isSaving && !state.saveError && !isTerminal) {
            showCancelDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink),
    ) {
        when {
            state.saveError -> SaveErrorContent(
                onRetry = onRetrySave,
                onDiscard = onDiscard,
            )

            state.isSaving || isTerminal -> SavingContent()

            else -> Crossfade(
                targetState = when {
                    state.isRestComplete -> WorkoutPhase.REST_COMPLETE
                    state.isResting -> WorkoutPhase.REST
                    else -> WorkoutPhase.EXERCISE
                },
                animationSpec = tween(durationMillis = 220),
                label = "workoutPhase",
            ) { phase ->
                when (phase) {
                    WorkoutPhase.REST, WorkoutPhase.REST_COMPLETE -> RestContent(
                        state = state,
                        onExit = { showCancelDialog = true },
                        onTogglePause = onTogglePause,
                        onAddRest = onAddRest,
                        onSkipRest = onSkipRest,
                    )

                    else -> ExerciseContent(
                        state = state,
                        onExit = { showCancelDialog = true },
                        onTogglePause = onTogglePause,
                        onCompleteSet = onCompleteSet,
                    )
                }
            }
        }
    }

    ConfirmDialog(
        visible = showCancelDialog,
        title = "¿Terminar entrenamiento?",
        message = "Se guardará como cancelada con las series completadas hasta ahora.",
        confirmLabel = "Terminar",
        dismissLabel = "Seguir entrenando",
        destructive = true,
        onConfirm = {
            showCancelDialog = false
            onCancel()
        },
        onDismiss = { showCancelDialog = false },
    )
}

@Composable
private fun ExerciseContent(
    state: WorkoutUiState,
    onExit: () -> Unit,
    onTogglePause: () -> Unit,
    onCompleteSet: () -> Unit,
) {
    val ringColor = if (state.isPaused) Ember else Lime
    val subtitle = if (state.isPaused) {
        "EN PAUSA · ${formatElapsed(state.elapsedSeconds)}"
    } else {
        "Serie ${state.setNumber}/${state.setCount} · ${formatElapsed(state.elapsedSeconds)}"
    }

    WorkoutFrame(
        progress = state.sessionProgress,
        ringColor = ringColor,
        title = state.exerciseName,
        subtitle = subtitle,
        subtitleColor = if (state.isPaused) Ember else Mist,
        isPaused = state.isPaused,
        pauseEnabled = true,
        onExit = onExit,
        onTogglePause = onTogglePause,
    ) {
        Text(
            text = state.targetValue,
            style = MaterialTheme.typography.numeralLarge.copy(
                fontSize = 48.sp,
                lineHeight = 50.sp,
            ),
            color = WarmWhite,
            maxLines = 1,
        )
        Text(
            text = state.targetUnit,
            style = MaterialTheme.typography.labelSmall,
            color = ringColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = if (state.isPaused) onTogglePause else onCompleteSet,
            enabled = state.isPaused || state.canCompleteSet,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = signalButtonColors(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 6.dp,
                vertical = 0.dp,
            ),
        ) {
            Text(
                text = when {
                    state.isPaused -> "REANUDAR"
                    state.isFinalSet -> "TERMINAR"
                    else -> "SERIE HECHA"
                },
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RestContent(
    state: WorkoutUiState,
    onExit: () -> Unit,
    onTogglePause: () -> Unit,
    onAddRest: () -> Unit,
    onSkipRest: () -> Unit,
) {
    val complete = state.isRestComplete
    val ringColor = when {
        complete -> Lime
        state.isPaused -> Ember
        else -> Lime
    }
    val title = when {
        complete -> "Descanso listo"
        state.isPaused -> "En pausa"
        else -> "Descanso"
    }
    val next = state.nextExerciseName?.let { name ->
        "Siguiente: $name · S${state.nextSetNumber}/${state.nextSetCount}"
    } ?: "Siguiente serie"

    WorkoutFrame(
        progress = if (complete) 1f else state.restProgress,
        ringColor = ringColor,
        title = title,
        subtitle = next,
        subtitleColor = if (state.isPaused) Ember else Mist,
        isPaused = state.isPaused,
        pauseEnabled = !complete,
        onExit = onExit,
        onTogglePause = onTogglePause,
    ) {
        Text(
            text = formatElapsed(state.restRemainingSeconds),
            style = MaterialTheme.typography.numeralLarge.copy(
                fontSize = 48.sp,
                lineHeight = 50.sp,
            ),
            color = if (state.isPaused) Ember else WarmWhite,
            maxLines = 1,
        )
        Text(
            text = if (complete) "A TU RITMO" else "MIN : SEG",
            style = MaterialTheme.typography.labelSmall,
            color = Mist,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = onAddRest,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = secondaryButtonColors(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 2.dp,
                    vertical = 0.dp,
                ),
            ) {
                Text(
                    text = "+${state.additionalRestSeconds}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Button(
                onClick = onSkipRest,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = signalButtonColors(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 2.dp,
                    vertical = 0.dp,
                ),
            ) {
                Text(
                    text = if (complete) "SEGUIR" else "SALTAR",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun WorkoutFrame(
    progress: Float,
    ringColor: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    isPaused: Boolean,
    pauseEnabled: Boolean,
    onExit: () -> Unit,
    onTogglePause: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ProgressRing(
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            color = ringColor,
            strokeWidth = 8.dp,
            showTicks = true,
            description = if (isPaused) "Entrenamiento en pausa" else "Progreso del entrenamiento",
        ) {}

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .fillMaxWidth(0.58f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = WarmWhite,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyExtraSmall,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 48.dp, bottom = 18.dp)
                .width(104.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            content()
        }

        FilledTonalIconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 7.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = GraphiteHigh,
                contentColor = WarmWhite,
            ),
        ) {
            CloseGlyph(
                modifier = Modifier.semantics {
                    contentDescription = "Terminar entrenamiento"
                },
                size = 18.dp,
            )
        }

        FilledTonalIconButton(
            onClick = onTogglePause,
            enabled = pauseEnabled,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 7.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = GraphiteHigh,
                contentColor = if (isPaused) Lime else WarmWhite,
            ),
        ) {
            if (isPaused) {
                PlayGlyph(
                    modifier = Modifier.semantics {
                        contentDescription = "Reanudar entrenamiento"
                    },
                    size = 19.dp,
                )
            } else {
                PauseGlyph(
                    modifier = Modifier.semantics {
                        contentDescription = "Pausar entrenamiento"
                    },
                    size = 19.dp,
                )
            }
        }
    }
}

@Composable
private fun SavingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SetSignalMark(height = 26.dp)
        Spacer(modifier = Modifier.height(14.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(34.dp),
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = Lime,
                trackColor = OutlineSubtle,
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Guardando sesión",
            style = MaterialTheme.typography.titleSmall,
            color = WarmWhite,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tu progreso se guarda en este reloj.",
            style = MaterialTheme.typography.bodyExtraSmall,
            color = Mist,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SaveErrorContent(
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No se pudo guardar",
            style = MaterialTheme.typography.titleMedium,
            color = Coral,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "El almacenamiento local no respondió. Puedes reintentar sin perder la sesión.",
            style = MaterialTheme.typography.bodyExtraSmall,
            color = Mist,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = signalButtonColors(),
        ) {
            Text("Reintentar")
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier.fillMaxWidth(),
            colors = dangerButtonColors(),
        ) {
            Text("Descartar sin guardar", maxLines = 1)
        }
    }
}
