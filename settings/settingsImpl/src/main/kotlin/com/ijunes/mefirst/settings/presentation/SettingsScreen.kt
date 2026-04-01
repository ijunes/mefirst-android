package com.ijunes.mefirst.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showRemovePinDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val flushTimeLabel = "%02d:%02d".format(uiState.flushHour, uiState.flushMinute)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingsSectionHeader("Daily Notes")
                ListItem(
                    headlineContent = { Text("Flush time") },
                    supportingContent = { Text(flushTimeLabel) },
                    leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    trailingContent = {
                        TextButton(onClick = { showTimePicker = true }) { Text("Edit") }
                    },
                )
                HorizontalDivider()
            }

            item {
                SettingsSectionHeader("Backup & Restore")
                ListItem(
                    headlineContent = { Text("Back up data") },
                    supportingContent = { Text("Export database and media to a ZIP file") },
                    leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                    modifier = Modifier.clickable { viewModel.startBackup() },
                )
                HorizontalDivider(Modifier.padding(start = 56.dp))
                ListItem(
                    headlineContent = { Text("Restore from backup") },
                    supportingContent = { Text("Replace all data from a ZIP backup") },
                    leadingContent = { Icon(Icons.Default.Restore, contentDescription = null) },
                    modifier = Modifier.clickable { showRestoreConfirmDialog = true },
                )
                HorizontalDivider()
            }

            item {
                SettingsSectionHeader("Security")
                if (uiState.hasPin) {
                    ListItem(
                        headlineContent = { Text("Change PIN") },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.clickable { showSetPinDialog = true },
                    )
                    HorizontalDivider(Modifier.padding(start = 56.dp))
                    ListItem(
                        headlineContent = { Text("Remove PIN") },
                        leadingContent = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                        modifier = Modifier.clickable { showRemovePinDialog = true },
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("Set app PIN") },
                        supportingContent = { Text("Require a PIN to open the app") },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.clickable { showSetPinDialog = true },
                    )
                }
                HorizontalDivider()
            }
        }

        if (uiState.isBackingUp || uiState.isRestoring) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.flushHour,
            initialMinute = uiState.flushMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set daily flush time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setFlushTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }

    if (showSetPinDialog) {
        PinSetDialog(
            hasExistingPin = uiState.hasPin,
            onVerifyExisting = { pin -> viewModel.verifyPin(pin) },
            onSetPin = { pin ->
                viewModel.setPin(pin)
                showSetPinDialog = false
            },
            onDismiss = { showSetPinDialog = false },
        )
    }

    if (showRemovePinDialog) {
        PinVerifyDialog(
            title = "Remove PIN",
            description = "Enter your current PIN to remove it.",
            onConfirm = { pin ->
                val success = viewModel.removePin(pin)
                if (success) showRemovePinDialog = false
                success
            },
            onDismiss = { showRemovePinDialog = false },
        )
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restore from backup?") },
            text = { Text("This will replace all current data and media. The app will restart automatically.") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    viewModel.startRestore()
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) { Text("Cancel") }
            },
        )
    }

    uiState.backupResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackupResult() },
            title = {
                Text(if (result is BackupResult.Success) "Backup complete" else "Backup failed")
            },
            text = {
                Text(
                    if (result is BackupResult.Success) "Your data was saved successfully."
                    else (result as BackupResult.Error).message
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissBackupResult() }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun PinSetDialog(
    hasExistingPin: Boolean,
    onVerifyExisting: (String) -> Boolean,
    onSetPin: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // step 0: verify current (only if hasExistingPin)
    // step 1: enter new PIN
    // step 2: confirm new PIN
    val startStep = if (hasExistingPin) 0 else 1
    var step by remember { mutableIntStateOf(startStep) }
    var pinInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val (title, label) = when (step) {
        0 -> "Change PIN" to "Current PIN"
        1 -> (if (hasExistingPin) "Change PIN" else "Set PIN") to "New PIN (4 digits)"
        else -> (if (hasExistingPin) "Change PIN" else "Set PIN") to "Confirm PIN"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinInput = it },
                    label = { Text(label) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (step) {
                        0 -> {
                            if (onVerifyExisting(pinInput)) {
                                step = 1
                                pinInput = ""
                                error = null
                            } else {
                                error = "Incorrect PIN"
                            }
                        }
                        1 -> {
                            if (pinInput.length < 4) {
                                error = "PIN must be 4 digits"
                            } else {
                                newPin = pinInput
                                pinInput = ""
                                error = null
                                step = 2
                            }
                        }
                        2 -> {
                            if (pinInput != newPin) {
                                error = "PINs do not match"
                                pinInput = ""
                            } else {
                                onSetPin(newPin)
                            }
                        }
                    }
                }
            ) { Text("Next") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun PinVerifyDialog(
    title: String,
    description: String,
    onConfirm: (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    var pinInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(description)
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinInput = it },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val success = onConfirm(pinInput)
                if (!success) {
                    error = "Incorrect PIN"
                    pinInput = ""
                }
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
