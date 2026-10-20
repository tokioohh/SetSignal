package com.example.setsignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.setsignal.domain.model.Routine
import com.example.setsignal.ui.components.BackHeader
import com.example.setsignal.ui.components.RoutineCard
import com.example.setsignal.ui.components.SectionLabel
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun RoutinesScreen(
    routines: List<Routine>,
    onBack: () -> Unit,
    onOpenRoutine: (String) -> Unit,
) {
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
                    title = "Rutinas",
                    subtitle = "Tres puntos de partida",
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Text(
                    text = "Elige una sesión, ajusta el descanso y empieza cuando estés listo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            item {
                SectionLabel(text = "Biblioteca local", modifier = Modifier.fillMaxWidth())
            }
            itemsIndexed(routines) { index, routine ->
                RoutineCard(
                    routine = routine,
                    index = index,
                    onClick = { onOpenRoutine(routine.id) },
                )
            }
            item {
                Text(
                    text = "Las rutinas se guardan en la app. No necesitan conexión ni cuenta.",
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = WarmWhite.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private fun androidx.wear.compose.foundation.lazy.ScalingLazyListScope.itemsIndexed(
    items: List<Routine>,
    content: @Composable (Int, Routine) -> Unit,
) {
    items.forEachIndexed { index, routine ->
        item(key = routine.id) { content(index, routine) }
    }
}
