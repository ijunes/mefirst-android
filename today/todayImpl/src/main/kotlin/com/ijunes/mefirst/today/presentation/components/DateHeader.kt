package com.ijunes.mefirst.today.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ijunes.mefirst.common.util.toDateString


/** Displays today's date centred at the top of the feed. */
@Composable
fun DateHeader() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textAlign = TextAlign.Center,
        fontWeight = Bold,
        color = MaterialTheme.colorScheme.secondary,
        text = "- " + System.currentTimeMillis().toDateString() + " -"
    )
}