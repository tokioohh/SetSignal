package com.example.setsignal.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.setsignal.ui.theme.OutlineSubtle
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sports-watch style progress ring with an outer tick track. The ring is intentionally the visual
 * anchor of the workout screen: one glance gives the current value and the session progress.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    trackColor: Color = OutlineSubtle,
    strokeWidth: Dp = 9.dp,
    showTicks: Boolean = true,
    description: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 240, easing = LinearEasing),
        label = "progressRing",
    )

    val semanticsModifier = Modifier.semantics {
        progressBarRangeInfo = ProgressBarRangeInfo(animatedProgress, 0f..1f)
        if (description != null) this.contentDescription = description
    }

    Box(
        modifier = modifier.then(semanticsModifier),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = size.minDimension / 2f
            val strokePx = strokeWidth.toPx()
            val tickLength = if (showTicks) 5.dp.toPx() else 0f
            val gap = if (showTicks) 7.dp.toPx() else 0f
            val radius = (outerRadius - tickLength - gap - strokePx / 2f).coerceAtLeast(1f)
            val center = Offset(size.width / 2f, size.height / 2f)

            if (showTicks) {
                val tickCount = 60
                for (index in 0 until tickCount) {
                    val angle = Math.toRadians((index * 360.0 / tickCount) - 90.0)
                    val cosAngle = cos(angle).toFloat()
                    val sinAngle = sin(angle).toFloat()
                    val start = Offset(
                        center.x + cosAngle * outerRadius,
                        center.y + sinAngle * outerRadius,
                    )
                    val end = Offset(
                        center.x + cosAngle * (outerRadius - tickLength),
                        center.y + sinAngle * (outerRadius - tickLength),
                    )
                    drawLine(
                        color = trackColor.copy(alpha = if (index % 5 == 0) 0.65f else 0.3f),
                        start = start,
                        end = end,
                        strokeWidth = 1.1.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

@Composable
fun MiniRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    trackColor: Color = OutlineSubtle,
    strokeWidth: Dp = 3.dp,
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 300),
        label = "miniRing",
    )
    Canvas(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(animatedProgress, 0f..1f)
        },
    ) {
        val strokePx = strokeWidth.toPx()
        val radius = size.minDimension / 2f - strokePx / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokePx),
        )
        if (animatedProgress > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}
