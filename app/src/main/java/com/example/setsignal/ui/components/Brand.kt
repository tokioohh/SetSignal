package com.example.setsignal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

/** Three ascending bars: three sets, one signal. */
@Composable
fun SetSignalMark(
    modifier: Modifier = Modifier,
    color: Color = Lime,
    height: Dp = 22.dp,
) {
    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(height * 0.14f),
    ) {
        listOf(0.45f, 0.7f, 1f).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(height * 0.17f)
                    .height(height * fraction)
                    .clip(RoundedCornerShape(height * 0.06f))
                    .background(color),
            )
        }
    }
}

@Composable
fun SetSignalWordmark(
    modifier: Modifier = Modifier,
    markColor: Color = Lime,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SetSignalMark(color = markColor, height = 22.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SET",
                color = WarmWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "SIGNAL",
                color = markColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun BrandTagline(modifier: Modifier = Modifier) {
    Text(
        text = "ENTRENA · DESCANSA · REPITE",
        modifier = modifier,
        style = MaterialTheme.typography.bodyExtraSmall,
        color = Mist,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SetSignalWordmark()
        Spacer(modifier = Modifier.height(8.dp))
        BrandTagline()
    }
}
