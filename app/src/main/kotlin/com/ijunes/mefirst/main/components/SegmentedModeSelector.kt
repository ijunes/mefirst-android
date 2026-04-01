package com.ijunes.mefirst.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijunes.mefirst.R
import com.ijunes.mefirst.ui.theme.AppTheme

@Composable
fun SegmentedModeSelector(
    isWorkMode: Boolean,
    modifier: Modifier = Modifier,
    onModeChanged: (Boolean) -> Unit
) {
    val containerColor = Color(0xFFF1F4FF)


    Row(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .clip(RoundedCornerShape(32.dp))
            .background(containerColor)
            .padding(2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeSegment(
            text = stringResource(R.string.mode_personal),
            iconRes = R.drawable.ic_personal,
            isSelected = !isWorkMode,
            selectedColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedContentColor = MaterialTheme.colorScheme.onPrimaryFixed,
            onClick = { onModeChanged(false) },
            modifier = Modifier.weight(1f)
        )

        ModeSegment(
            text = stringResource(R.string.mode_work),
            iconRes = R.drawable.ic_work,
            isSelected = isWorkMode,
            selectedColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedContentColor = MaterialTheme.colorScheme.onPrimaryFixed,
            onClick = { onModeChanged(true) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeSegment(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    selectedColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        label = "backgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
        label = "contentColor"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SegmentedModeSelectorPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SegmentedModeSelector(
                isWorkMode = false,
                onModeChanged = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SegmentedModeSelectorWorkPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SegmentedModeSelector(
                isWorkMode = true,
                onModeChanged = {}
            )
        }
    }
}
