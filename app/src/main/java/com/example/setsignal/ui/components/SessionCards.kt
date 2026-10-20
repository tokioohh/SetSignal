package com.example.setsignal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.ui.formatClockTime
import com.example.setsignal.ui.formatElapsed
import com.example.setsignal.ui.formatHistoryDay
import com.example.setsignal.ui.formatSetRatio
import com.example.setsignal.ui.theme.Coral
import com.example.setsignal.ui.theme.CoralContainer
import com.example.setsignal.ui.theme.GraphiteHigh
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.LimeContainer
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun SessionCard(
    session: WorkoutSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detailed: Boolean = false,
) {
    val accent = if (session.wasCompleted) Lime else Coral
    val container = if (session.wasCompleted) LimeContainer else CoralContainer

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GraphiteHigh),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 12.dp,
            vertical = 10.dp,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                MiniRing(
                    progress = session.setProgress,
                    modifier = Modifier.size(38.dp),
                    color = accent,
                    strokeWidth = 3.dp,
                )
                Text(
                    text = "${(session.setProgress * 100).toInt()}",
                    style = MaterialTheme.typography.numeralExtraSmall,
                    color = WarmWhite,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.routineName,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(formatHistoryDay(session.startedAtEpochMillis))
                        append(" · ")
                        append(formatElapsed(session.durationSeconds))
                        append(" · ")
                        append(formatClockTime(session.finishedAtEpochMillis))
                    },
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (detailed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accent),
                        )
                        Text(
                            text = if (session.wasCompleted) "Completada" else "Cancelada",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                        )
                        Text(
                            text = "· ${formatSetRatio(session.completedSets, session.plannedSets)} series",
                            style = MaterialTheme.typography.bodyExtraSmall,
                            color = Mist,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    accent: Color = Lime,
) {
    Panel(modifier = modifier, containerColor = GraphiteHigh) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SetSignalMark(color = accent, height = 28.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmWhite,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    accent: Color = Lime,
) {
    ActionCard(
        label = label,
        subtitle = subtitle,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        accent = accent,
    )
}

@Composable
fun ActionCard(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    accent: Color = Lime,
    iconSize: Dp = 34.dp,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GraphiteHigh),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 12.dp,
            vertical = 9.dp,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.wear.compose.material3.LocalContentColor provides accent,
                ) {
                    icon()
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmWhite,
                    maxLines = 1,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Mist,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
