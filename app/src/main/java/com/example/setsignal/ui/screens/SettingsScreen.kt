package com.example.setsignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.setsignal.domain.model.UserSettings
import com.example.setsignal.feature.settings.SettingsViewModel
import com.example.setsignal.ui.components.BackHeader
import com.example.setsignal.ui.components.BoltGlyph
import com.example.setsignal.ui.components.ConfirmDialog
import com.example.setsignal.ui.components.Panel
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.components.ValueStepper
import com.example.setsignal.ui.components.dangerButtonColors
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val listState = rememberScalingLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }

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
                    title = "Ajustes",
                    subtitle = "Preferencias locales",
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ValueStepper(
                    label = "Descanso por defecto",
                    valueLabel = "${settings.defaultRestSeconds} s",
                    supporting = "Se usa al abrir una rutina",
                    onDecrease = {
                        viewModel.changeDefaultRest(-UserSettings.REST_STEP_SECONDS)
                    },
                    onIncrease = {
                        viewModel.changeDefaultRest(UserSettings.REST_STEP_SECONDS)
                    },
                    decreaseEnabled = settings.defaultRestSeconds > UserSettings.MIN_REST_SECONDS,
                    increaseEnabled = settings.defaultRestSeconds < UserSettings.MAX_REST_SECONDS,
                )
            }
            item {
                ValueStepper(
                    label = "Descanso adicional",
                    valueLabel = "+${settings.additionalRestSeconds} s",
                    supporting = "Botón de tiempo durante el descanso",
                    onDecrease = {
                        viewModel.changeAdditionalRest(-UserSettings.ADDITIONAL_REST_STEP_SECONDS)
                    },
                    onIncrease = {
                        viewModel.changeAdditionalRest(UserSettings.ADDITIONAL_REST_STEP_SECONDS)
                    },
                    decreaseEnabled =
                        settings.additionalRestSeconds > UserSettings.MIN_ADDITIONAL_REST_SECONDS,
                    increaseEnabled =
                        settings.additionalRestSeconds < UserSettings.MAX_ADDITIONAL_REST_SECONDS,
                )
            }
            item {
                SwitchButton(
                    checked = settings.hapticsEnabled,
                    onCheckedChange = viewModel::setHapticsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    icon = { BoltGlyph(size = 20.dp) },
                    label = { Text("Vibración") },
                    secondaryLabel = { Text("Aviso al terminar el descanso") },
                )
            }
            item {
                SectionLabel(text = "Datos", modifier = Modifier.fillMaxWidth())
            }
            item {
                Panel {
                    Text(
                        text = "El historial se guarda solo en este reloj. No hay cuenta, nube ni sincronización.",
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = dangerButtonColors(),
                    ) {
                        Text("Borrar historial")
                    }
                }
            }
            item {
                SectionLabel(text = "Acerca de", modifier = Modifier.fillMaxWidth())
            }
            item {
                Panel {
                    Text(
                        text = "SetSignal 0.1.0",
                        style = MaterialTheme.typography.titleSmall,
                        color = WarmWhite,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Entrenamiento por series, sin cuenta ni conexión.",
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "Las rutinas incluidas son ejemplos. Ajusta el esfuerzo a tu nivel y consulta a un profesional si tienes dudas.",
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                        textAlign = TextAlign.Start,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    ConfirmDialog(
        visible = showClearDialog,
        title = "¿Borrar todo el historial?",
        message = "Se eliminarán todas las sesiones guardadas en este reloj. Esta acción no se puede deshacer.",
        confirmLabel = "Borrar historial",
        dismissLabel = "Cancelar",
        destructive = true,
        onConfirm = {
            viewModel.clearHistory()
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false },
    )
}
