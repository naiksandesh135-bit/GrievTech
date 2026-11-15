package com.grievtech.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.grievtech.app.R
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTextField
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.components.LanguageSelector
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing
import com.grievtech.app.util.LanguageManager

@Composable
fun TrackEntryScreen(
    languageManager: LanguageManager?,
    onTrackSubmitted: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var trackingIdInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val trackingPattern = Regex("^GT-\\d{6}$")

    Scaffold(
        topBar = {
            GrievTopBar(
                title = stringResource(R.string.track_entry_title),
                subtitle = stringResource(R.string.track_entry_subtitle),
                onBackClick = onBackClick,
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
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Track Grievance Form" },
                shape = Shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.track_entry_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = stringResource(R.string.track_entry_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    GrievTextField(
                        value = trackingIdInput,
                        onValueChange = { input ->
                            val uppercase = input.uppercase()
                            trackingIdInput = uppercase
                            if (errorMessage != null) errorMessage = null
                        },
                        label = stringResource(R.string.tracking_id_label),
                        placeholder = stringResource(R.string.tracking_id_hint),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        errorText = errorMessage,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            if (trackingPattern.matches(trackingIdInput)) {
                                onTrackSubmitted(trackingIdInput)
                            } else {
                                errorMessage = "Tracking ID must be in format GT-XXXXXX (e.g. GT-123456)."
                            }
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Tracking ID Field" }
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    GrievButton(
                        text = stringResource(R.string.track_grievance),
                        onClick = {
                            keyboardController?.hide()
                            if (trackingPattern.matches(trackingIdInput)) {
                                onTrackSubmitted(trackingIdInput)
                            } else {
                                errorMessage = "Tracking ID must be in format GT-XXXXXX (e.g. GT-123456)."
                            }
                        },
                        type = GrievButtonType.PRIMARY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Track Grievance Button" }
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    GrievButton(
                        text = stringResource(R.string.back),
                        onClick = onBackClick,
                        type = GrievButtonType.OUTLINED,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackEntryScreenPreview() {
    GrievTechTheme {
        TrackEntryScreen(
            languageManager = null,
            onTrackSubmitted = {},
            onBackClick = {}
        )
    }
}
