package com.example.setsignal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import com.example.setsignal.ui.theme.Lime
import com.example.setsignal.ui.theme.Mist
import com.example.setsignal.ui.theme.WarmWhite

/**
 * Full-screen Wear confirmation dialog. Using full-width buttons (instead of the default icon
 * buttons) keeps destructive actions explicit on a small round screen.
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    Dialog(visible = visible, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SetSignalMark(color = if (destructive) MaterialTheme.colorScheme.error else Lime)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WarmWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Mist,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = if (destructive) dangerButtonColors() else signalButtonColors(),
            ) {
                Text(confirmLabel)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = secondaryButtonColors(),
            ) {
                Text(dismissLabel)
            }
        }
    }
}
