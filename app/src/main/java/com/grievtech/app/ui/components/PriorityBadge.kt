package com.grievtech.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grievtech.app.data.model.Priority
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing

@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val isEmergency = priority == Priority.EMERGENCY
    val bgColor = if (isEmergency) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isEmergency) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val border = if (isEmergency) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

    Surface(
        modifier = modifier,
        shape = Shapes.small,
        color = bgColor,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEmergency) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = Spacing.xs)
                )
            }
            Text(
                text = priority.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PriorityBadgeLightPreview() {
    GrievTechTheme(darkTheme = false) {
        Row(modifier = Modifier.padding(16.dp)) {
            PriorityBadge(priority = Priority.NORMAL, modifier = Modifier.padding(end = 8.dp))
            PriorityBadge(priority = Priority.EMERGENCY)
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun PriorityBadgeDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        Row(modifier = Modifier.padding(16.dp)) {
            PriorityBadge(priority = Priority.NORMAL, modifier = Modifier.padding(end = 8.dp))
            PriorityBadge(priority = Priority.EMERGENCY)
        }
    }
}
