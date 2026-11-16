package com.grievtech.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grievtech.app.data.model.ComplaintStatus
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing

@Composable
fun StatusChip(
    status: ComplaintStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ComplaintStatus.SUBMITTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ComplaintStatus.ASSIGNED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.tertiary
        ComplaintStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.secondary
        ComplaintStatus.RESOLVED -> Color(0xFF4ADE80).copy(alpha = 0.15f) to Color(0xFF4ADE80)
        ComplaintStatus.CLOSED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) to MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier,
        shape = Shapes.small,
        color = bgColor
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun StatusChipLightPreview() {
    GrievTechTheme(darkTheme = false) {
        Row(modifier = Modifier.padding(16.dp)) {
            ComplaintStatus.entries.forEach { status ->
                StatusChip(status = status, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun StatusChipDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        Row(modifier = Modifier.padding(16.dp)) {
            ComplaintStatus.entries.forEach { status ->
                StatusChip(status = status, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}
