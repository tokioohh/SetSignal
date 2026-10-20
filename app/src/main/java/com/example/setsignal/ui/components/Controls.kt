package com.example.setsignal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.setsignal.ui.theme.Coral
import com.example.setsignal.ui.theme.Graphite
import com.example.setsignal.ui.theme.GraphiteHigh
import com.example.setsignal.ui.theme.Ink
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.OutlineSubtle
import com.example.setsignal.ui.theme.WarmWhite

@Composable
fun StatusPill(
    text: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(contentColor = WarmWhite),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WarmWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = Mist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Mist,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
    )
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    containerColor: Color = Graphite,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        content = content,
    )
}

@Composable
fun ValueStepper(
    label: String,
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    decreaseEnabled: Boolean = true,
    increaseEnabled: Boolean = true,
) {
    Panel(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Mist,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onDecrease,
                enabled = decreaseEnabled,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = GraphiteHigh,
                    contentColor = WarmWhite,
                ),
            ) {
                MinusGlyph(
                    modifier = Modifier
                        .size(18.dp)
                        .semantics { contentDescription = "Disminuir $label" },
                    color = WarmWhite,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                if (supporting != null) {
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodyExtraSmall,
                        color = Mist,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
            FilledTonalIconButton(
                onClick = onIncrease,
                enabled = increaseEnabled,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = GraphiteHigh,
                    contentColor = Lime,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Aumentar $label",
                )
            }
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = WarmWhite,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Mist,
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            maxLines = 1,
        )
    }
}

@Composable
fun Disclaimer(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyExtraSmall,
        color = Mist,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun signalButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Lime,
    contentColor = Ink,
    disabledContainerColor = Lime.copy(alpha = 0.2f),
    disabledContentColor = Ink.copy(alpha = 0.5f),
)

@Composable
fun secondaryButtonColors() = ButtonDefaults.buttonColors(
    containerColor = GraphiteHigh,
    contentColor = WarmWhite,
    disabledContainerColor = GraphiteHigh.copy(alpha = 0.5f),
    disabledContentColor = Mist,
)

@Composable
fun dangerButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Coral.copy(alpha = 0.18f),
    contentColor = Coral,
)

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(OutlineSubtle),
    )
}

@Suppress("unused")
private val RoundedLarge = RoundedCornerShape(26.dp)
