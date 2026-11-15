package com.grievtech.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.grievtech.app.data.model.Profile
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.components.StatCard
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Spacing

@Composable
fun CitizenHomeScreen(
    userProfile: Profile?,
    onLogoutClick: () -> Unit,
    onSubmitComplaintClick: () -> Unit,
    onTrackComplaintsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            GrievTopBar(
                title = "Citizen Portal",
                subtitle = userProfile?.let { "Welcome, ${it.fullName}" } ?: "Welcome, Citizen",
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
            if (userProfile != null) {
                Text(
                    text = "Signed in as: ${userProfile.fullName} (${userProfile.phone})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            GrievButton(
                text = "+ Submit New Grievance",
                onClick = onSubmitComplaintClick,
                type = GrievButtonType.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            GrievButton(
                text = "Track Submitted Complaints",
                onClick = onTrackComplaintsClick,
                type = GrievButtonType.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Your Activity Summary",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatCard(
                    title = "Submitted",
                    value = "5",
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Resolved",
                    value = "3",
                    icon = Icons.Default.CheckCircle,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            StatCard(
                title = "Emergency Alerts Raised",
                value = "1",
                icon = Icons.Default.AddAlert,
                iconTint = MaterialTheme.colorScheme.error,
                subtitle = "Active priority response"
            )

            Spacer(modifier = Modifier.weight(1f))

            GrievButton(
                text = "Log Out",
                onClick = onLogoutClick,
                type = GrievButtonType.OUTLINED,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CitizenHomeScreenPreview() {
    GrievTechTheme {
        CitizenHomeScreen(
            userProfile = Profile(fullName = "Jane Doe", phone = "+91 9876543210", role = UserRole.CITIZEN),
            onLogoutClick = {},
            onSubmitComplaintClick = {},
            onTrackComplaintsClick = {}
        )
    }
}
