package com.grievtech.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.ComplaintStatus
import com.grievtech.app.data.model.Priority
import com.grievtech.app.ui.components.GrievTextField
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.components.PriorityBadge
import com.grievtech.app.ui.components.StatusChip
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing

@Composable
fun TrackComplaintScreen(
    onComplaintClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val mockComplaints = listOf(
        Complaint(id = "c101", trackingId = "TRK1001", userId = "u1", category = Category.POTHOLES, title = "Pothole on Main St", description = "Deep pothole causing traffic", priority = Priority.NORMAL, status = ComplaintStatus.SUBMITTED, address = "Main St, Sector 4", createdAt = "2026-03-01"),
        Complaint(id = "c102", trackingId = "TRK1002", userId = "u1", category = Category.STREETLIGHTS, title = "Streetlight broken", description = "Lamp post non-functional at night", priority = Priority.EMERGENCY, status = ComplaintStatus.IN_PROGRESS, address = "Park View Ave", createdAt = "2026-03-02"),
        Complaint(id = "c103", trackingId = "TRK1003", userId = "u1", category = Category.GARBAGE, title = "Garbage overflow", description = "Bin not cleared for 3 days", priority = Priority.NORMAL, status = ComplaintStatus.RESOLVED, address = "Market Square", createdAt = "2026-02-28")
    )

    Scaffold(
        topBar = {
            GrievTopBar(
                title = "Track Grievances",
                subtitle = "Live Status Updates",
                onBackClick = onBackClick
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg)
        ) {
            GrievTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search by ID or Title",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(mockComplaints) { complaint ->
                    ComplaintItemCard(
                        complaint = complaint,
                        onClick = { onComplaintClick(complaint.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintItemCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = complaint.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                PriorityBadge(priority = complaint.priority)
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = complaint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = complaint.status)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = complaint.createdAt ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackComplaintScreenPreview() {
    GrievTechTheme {
        TrackComplaintScreen(
            onComplaintClick = {},
            onBackClick = {}
        )
    }
}
