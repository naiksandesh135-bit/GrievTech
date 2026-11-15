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
import com.grievtech.app.ui.theme.StatusAssignedBg
import com.grievtech.app.ui.theme.StatusAssignedText
import com.grievtech.app.ui.theme.StatusClosedBg
import com.grievtech.app.ui.theme.StatusClosedText
import com.grievtech.app.ui.theme.StatusInProgressBg
import com.grievtech.app.ui.theme.StatusInProgressText
import com.grievtech.app.ui.theme.StatusResolvedBg
import com.grievtech.app.ui.theme.StatusResolvedText
import com.grievtech.app.ui.theme.StatusSubmittedBg
import com.grievtech.app.ui.theme.StatusSubmittedText

@Composable
fun StatusChip(
    status: ComplaintStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ComplaintStatus.SUBMITTED -> StatusSubmittedBg to StatusSubmittedText
        ComplaintStatus.ASSIGNED -> StatusAssignedBg to StatusAssignedText
        ComplaintStatus.IN_PROGRESS -> StatusInProgressBg to StatusInProgressText
        ComplaintStatus.RESOLVED -> StatusResolvedBg to StatusResolvedText
        ComplaintStatus.CLOSED -> StatusClosedBg to StatusClosedText
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

@Preview(showBackground = true)
@Composable
fun StatusChipPreview() {
    GrievTechTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            ComplaintStatus.entries.forEach { status ->
                StatusChip(status = status, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}
