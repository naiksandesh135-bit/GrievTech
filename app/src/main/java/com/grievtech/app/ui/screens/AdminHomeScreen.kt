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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
fun AdminHomeScreen(
    userProfile: Profile?,
    onLogoutClick: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            GrievTopBar(
                title = "Admin Dashboard",
                subtitle = userProfile?.let { "Administrator: ${it.fullName}" } ?: "Municipal Grievance Management",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
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
                    text = "Role: ${userProfile.role.label} | Admin ID: ${userProfile.id.take(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatCard(
                    title = "Pending",
                    value = "24",
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Emergency",
                    value = "5",
                    icon = Icons.Default.Warning,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            StatCard(
                title = "Resolved Today",
                value = "18",
                icon = Icons.Default.CheckCircle,
                iconTint = MaterialTheme.colorScheme.secondary,
                subtitle = "92% SLA Compliance"
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Administrative Tools",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            GrievButton(
                text = "View Analytics & SDG Impact (MPAndroidChart)",
                onClick = onNavigateToAnalytics,
                type = GrievButtonType.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            GrievButton(
                text = "Log Out Administrator",
                onClick = onLogoutClick,
                type = GrievButtonType.OUTLINED,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    GrievTechTheme {
        AdminHomeScreen(
            userProfile = Profile(id = "adm_99", fullName = "Admin User", role = UserRole.ADMIN),
            onLogoutClick = {},
            onNavigateToAnalytics = {},
            onBackClick = {}
        )
    }
}
