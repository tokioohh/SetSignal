package com.example.setsignal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.LocalContentColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PauseGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val barWidth = this.size.width * 0.2f
        val barHeight = this.size.height * 0.62f
        val gap = this.size.width * 0.16f
        val top = (this.size.height - barHeight) / 2f
        val radius = CornerRadius(barWidth * 0.35f, barWidth * 0.35f)
        drawRoundRect(
            color = color,
            topLeft = Offset(this.size.width / 2f - gap / 2f - barWidth, top),
            size = Size(barWidth, barHeight),
            cornerRadius = radius,
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(this.size.width / 2f + gap / 2f, top),
            size = Size(barWidth, barHeight),
            cornerRadius = radius,
        )
    }
}

@Composable
fun MinusGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * 0.1f
        val inset = this.size.minDimension * 0.26f
        drawLine(
            color = color,
            start = Offset(inset, this.size.height / 2f),
            end = Offset(this.size.width - inset, this.size.height / 2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun PlayGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.36f, this@Canvas.size.height * 0.22f)
            lineTo(this@Canvas.size.width * 0.78f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.36f, this@Canvas.size.height * 0.78f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun CloseGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * 0.09f
        val inset = this.size.minDimension * 0.28f
        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(this.size.width - inset, this.size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(this.size.width - inset, inset),
            end = Offset(inset, this.size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun StopGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val side = this.size.minDimension * 0.54f
        val topLeft = Offset((this.size.width - side) / 2f, (this.size.height - side) / 2f)
        drawRoundRect(
            color = color,
            topLeft = topLeft,
            size = Size(side, side),
            cornerRadius = CornerRadius(side * 0.24f, side * 0.24f),
        )
    }
}

@Composable
fun ClockGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * 0.085f
        val radius = this.size.minDimension / 2f - stroke
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(color = color, radius = radius, style = Stroke(width = stroke), center = center)
        drawLine(
            color = color,
            start = center,
            end = center + Offset(0f, -radius * 0.55f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = center,
            end = center + Offset(radius * 0.42f, 0f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun TimerGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * 0.075f
        val radius = this.size.minDimension * 0.33f
        val center = Offset(this.size.width / 2f, this.size.height * 0.58f)
        drawCircle(color = color, radius = radius, style = Stroke(width = stroke), center = center)
        drawLine(
            color = color,
            start = center,
            end = center + Offset(0f, -radius * 0.6f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(this.size.width / 2f, this.size.height * 0.12f),
            end = Offset(this.size.width / 2f, this.size.height * 0.22f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        val handAngle = Math.toRadians(-50.0)
        drawLine(
            color = color,
            start = center,
            end = center + Offset(
                (cos(handAngle) * radius * 0.6).toFloat(),
                (sin(handAngle) * radius * 0.6).toFloat(),
            ),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun BoltGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val path = Path().apply {
            moveTo(width * 0.57f, height * 0.08f)
            lineTo(width * 0.24f, height * 0.54f)
            lineTo(width * 0.48f, height * 0.54f)
            lineTo(width * 0.4f, height * 0.92f)
            lineTo(width * 0.78f, height * 0.42f)
            lineTo(width * 0.54f, height * 0.42f)
            close()
        }
        drawPath(path = path, color = color)
    }
}
