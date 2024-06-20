package com.example.productivitygame.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.productivitygame.R

@Composable
fun ConfirmationAlert(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = { 
            TextButton(onClick = onConfirmRequest) {
                Text(text = stringResource(R.string.dialog_confirm_text),
                    style = MaterialTheme.typography.labelSmall)
            }
        },
        title = { Text(text = title) },
        text = { Text(text = description) },
        dismissButton = { 
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.dialog_dissmiss_text),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}