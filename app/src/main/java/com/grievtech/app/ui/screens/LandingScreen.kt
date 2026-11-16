package com.grievtech.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grievtech.app.R
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.components.LanguageSelector
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.PrimaryPurple
import com.grievtech.app.ui.theme.SecondaryBlue
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing
import com.grievtech.app.util.LanguageManager

data class IssueCategoryCardData(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val gradientColors: List<androidx.compose.ui.graphics.Color>
)

@Composable
fun LandingScreen(
    languageManager: LanguageManager?,
    onNavigateToLogin: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val issueCategories = listOf(
        IssueCategoryCardData(
            titleRes = R.string.issue_potholes_title,
            descRes = R.string.issue_potholes_desc,
            icon = Icons.Default.Build,
            gradientColors = listOf(PrimaryPurple, SecondaryBlue)
        ),
        IssueCategoryCardData(
            titleRes = R.string.issue_streetlights_title,
            descRes = R.string.issue_streetlights_desc,
            icon = Icons.Default.Lightbulb,
            gradientColors = listOf(SecondaryBlue, PrimaryPurple)
        ),
        IssueCategoryCardData(
            titleRes = R.string.issue_garbage_title,
            descRes = R.string.issue_garbage_desc,
            icon = Icons.Default.Delete,
            gradientColors = listOf(PrimaryPurple, MaterialTheme.colorScheme.tertiary)
        ),
        IssueCategoryCardData(
            titleRes = R.string.issue_water_title,
            descRes = R.string.issue_water_desc,
            icon = Icons.Default.WaterDrop,
            gradientColors = listOf(SecondaryBlue, MaterialTheme.colorScheme.primary)
        ),
        IssueCategoryCardData(
            titleRes = R.string.issue_electricity_title,
            descRes = R.string.issue_electricity_desc,
            icon = Icons.Default.Bolt,
            gradientColors = listOf(PrimaryPurple, SecondaryBlue)
        ),
        IssueCategoryCardData(
            titleRes = R.string.issue_other_title,
            descRes = R.string.issue_other_desc,
            icon = Icons.Default.AssignmentTurnedIn,
            gradientColors = listOf(SecondaryBlue, PrimaryPurple)
        )
    )

    Scaffold(
        topBar = {
            GrievTopBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.app_tagline),
                actions = {
                    if (languageManager != null) {
                        LanguageSelector(languageManager = languageManager)
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
                .verticalScroll(scrollState)
        ) {
            // Gradient Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryPurple, SecondaryBlue)
                        )
                    )
                    .padding(Spacing.lg)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape),
                        color = Color.Black,
                        shadowElevation = 8.dp
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_logo_full),
                            contentDescription = "GrievTech logo",
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = stringResource(R.string.landing_hero_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = stringResource(R.string.landing_hero_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // 4 Hero Actions: Register, Citizen Sign In, Admin Access, Track Grievance
                    GrievButton(
                        text = stringResource(R.string.landing_register_btn),
                        onClick = onNavigateToRegister,
                        type = GrievButtonType.PRIMARY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Register Account" }
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    GrievButton(
                        text = stringResource(R.string.landing_citizen_login_btn),
                        onClick = { onNavigateToLogin("CITIZEN") },
                        type = GrievButtonType.OUTLINED,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Citizen Sign In" }
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    GrievButton(
                        text = stringResource(R.string.landing_admin_login_btn),
                        onClick = { onNavigateToLogin("ADMIN") },
                        type = GrievButtonType.OUTLINED,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Administrator Access" }
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    GrievButton(
                        text = stringResource(R.string.landing_track_btn),
                        onClick = onNavigateToTrack,
                        type = GrievButtonType.SECONDARY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Track Grievance with ID" }
                    )
                }
            }

            // "Issues We Handle" Section
            Column(modifier = Modifier.padding(vertical = Spacing.lg)) {
                Text(
                    text = stringResource(R.string.issues_section_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )

                Text(
                    text = stringResource(R.string.issues_section_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(issueCategories) { category ->
                        IssueCategoryCard(
                            data = category,
                            onClick = onNavigateToRegister
                        )
                    }
                }
            }

            // How It Works Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            ) {
                Text(
                    text = stringResource(R.string.how_it_works_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                HowItWorksStep(
                    number = "1",
                    title = stringResource(R.string.step_1_title),
                    description = stringResource(R.string.step_1_desc)
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                HowItWorksStep(
                    number = "2",
                    title = stringResource(R.string.step_2_title),
                    description = stringResource(R.string.step_2_desc)
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                HowItWorksStep(
                    number = "3",
                    title = stringResource(R.string.step_3_title),
                    description = stringResource(R.string.step_3_desc)
                )
            }

            // Impact Strip Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                shape = Shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.impact_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ImpactItem(
                            value = stringResource(R.string.impact_stat_1),
                            label = stringResource(R.string.impact_stat_1_label)
                        )
                        ImpactItem(
                            value = stringResource(R.string.impact_stat_2),
                            label = stringResource(R.string.impact_stat_2_label)
                        )
                        ImpactItem(
                            value = stringResource(R.string.impact_stat_3),
                            label = stringResource(R.string.impact_stat_3_label)
                        )
                    }
                }
            }

            // Footer with SDG Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl, start = Spacing.lg, end = Spacing.lg),
                horizontalArrangement = Arrangement.Center
            ) {
                SdgChip(title = stringResource(R.string.sdg_11_title))
                Spacer(modifier = Modifier.width(Spacing.sm))
                SdgChip(title = stringResource(R.string.sdg_16_title))
            }
        }
    }
}

@Composable
private fun IssueCategoryCard(
    data: IssueCategoryCardData,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "CardScale")

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = Shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = data.gradientColors))
                .padding(Spacing.md)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )

                Column {
                    Text(
                        text = stringResource(data.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = stringResource(data.descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun HowItWorksStep(
    number: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = Shapes.small,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ImpactItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SdgChip(title: String) {
    Surface(
        shape = Shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun LandingScreenLightPreview() {
    GrievTechTheme(darkTheme = false) {
        LandingScreen(
            languageManager = null,
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            onNavigateToTrack = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun LandingScreenDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        LandingScreen(
            languageManager = null,
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            onNavigateToTrack = {}
        )
    }
}
