package com.ijunes.mefirst.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ijunes.mefirst.ui.theme.AppTheme

@Preview
@Composable
fun ModeToggleActionPreview(){
    AppTheme {
        ModeToggleAction(
            checked = false,
            onCheckChangedListener = {}
        )
    }
}

@Composable
fun ModeToggleAction(
    checked: Boolean,
    onCheckChangedListener: (Boolean) -> Unit
){
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Personal mode"
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckChangedListener
        )
        Icon(
            imageVector = Icons.Default.Work,
            contentDescription = "Work mode"
        )

    }
}