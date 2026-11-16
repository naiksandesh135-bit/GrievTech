package com.grievtech.app.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.grievtech.app.R
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.MediaType
import com.grievtech.app.data.model.Priority
import com.grievtech.app.ui.components.ErrorBanner
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTextField
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.components.LanguageSelector
import com.grievtech.app.ui.submit.SubmissionState
import com.grievtech.app.ui.submit.SubmitViewModel
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.ErrorContainerRed
import com.grievtech.app.ui.theme.ErrorRed
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing
import com.grievtech.app.util.LanguageManager
import com.grievtech.app.util.LocationMode
import com.grievtech.app.util.labelResource
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubmitComplaintScreen(
    viewModel: SubmitViewModel,
    languageManager: LanguageManager?,
    onNavigateToTrack: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLangCode by languageManager?.currentLanguageCode?.collectAsState(initial = "en") ?: remember { mutableStateOf("en") }

    BackHandler(enabled = uiState.isDirty) {
        viewModel.setShowConfirmLeaveDialog(true)
    }

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            val file = tempPhotoFile
            if (file != null && file.exists()) {
                viewModel.onCameraPhotoCaptured(file.absolutePath, tempPhotoUri!!)
            } else {
                viewModel.onMediaSelected(tempPhotoUri!!)
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onMediaSelected(uri)
        }
    }

    val mediaLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        documentPickerLauncher.launch(arrayOf("image/*", "video/*", "application/pdf"))
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceDictation(currentLangCode)
        } else {
            Toast.makeText(context, "Microphone permission required for voice dictation.", Toast.LENGTH_SHORT).show()
        }
    }

    if (uiState.submissionState is SubmissionState.Success) {
        val successState = uiState.submissionState as SubmissionState.Success
        SubmissionSuccessContent(
            trackingId = successState.trackingId,
            onTrackClick = { onNavigateToTrack(successState.trackingId) },
            onHomeClick = onNavigateHome,
            modifier = modifier
        )
        return
    }

    Scaffold(
        topBar = {
            GrievTopBar(
                title = stringResource(R.string.submit_grievance_title),
                subtitle = stringResource(R.string.submit_grievance_subtitle),
                onBackClick = {
                    if (uiState.isDirty) {
                        viewModel.setShowConfirmLeaveDialog(true)
                    } else {
                        onBackClick()
                    }
                },
                actions = {
                    if (languageManager != null) {
                        LanguageSelector(languageManager = languageManager)
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(Spacing.lg)
            ) {
                if (uiState.submissionState is SubmissionState.Error) {
                    val errorMsg = (uiState.submissionState as SubmissionState.Error).message
                    ErrorBanner(
                        message = errorMsg,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                // 1. Category Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = stringResource(R.string.category_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Category.entries.forEach { cat ->
                                FilterChip(
                                    selected = uiState.category == cat,
                                    onClick = { viewModel.onCategoryChanged(cat) },
                                    label = {
                                        Text(
                                            text = cat.labelResource(),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (cat) {
                                                Category.POTHOLES -> Icons.Default.Build
                                                Category.STREETLIGHTS -> Icons.Default.Lightbulb
                                                Category.GARBAGE -> Icons.Default.Delete
                                                Category.WATER -> Icons.Default.WaterDrop
                                                Category.ELECTRICITY -> Icons.Default.Bolt
                                                Category.SANITATION -> Icons.Default.CleaningServices
                                                Category.OTHER -> Icons.Default.AssignmentTurnedIn
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 2. Title & Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = stringResource(R.string.title_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        GrievTextField(
                            value = uiState.title,
                            onValueChange = viewModel::onTitleChanged,
                            label = stringResource(R.string.title_label),
                            placeholder = stringResource(R.string.title_placeholder),
                            errorText = uiState.titleError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${uiState.title.length}/80",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.End).padding(top = Spacing.xxs)
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.desc_label),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            OutlinedIconButton(
                                onClick = {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.speak_desc),
                                    tint = if (uiState.isVoiceDictating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))
                        GrievTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChanged,
                            label = stringResource(R.string.desc_label),
                            placeholder = stringResource(R.string.desc_placeholder),
                            errorText = uiState.descriptionError,
                            singleLine = false,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${uiState.description.length}/500 (min 20)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.description.length < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.End).padding(top = Spacing.xxs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 3. Priority Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = stringResource(R.string.priority_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            FilterChip(
                                selected = uiState.priority == Priority.NORMAL,
                                onClick = { viewModel.onPriorityChanged(Priority.NORMAL) },
                                label = { Text(stringResource(R.string.priority_normal)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.priority == Priority.EMERGENCY,
                                onClick = { viewModel.onPriorityChanged(Priority.EMERGENCY) },
                                label = { Text(stringResource(R.string.priority_emergency)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (uiState.priority == Priority.EMERGENCY) {
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorContainerRed),
                                shape = Shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(
                                        text = stringResource(R.string.emergency_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 4. Evidence Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = stringResource(R.string.evidence_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        // Live Location Mode Banner
                        Surface(
                            shape = Shapes.small,
                            color = if (uiState.locationMode is LocationMode.DeviceFix) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.locationMode is LocationMode.DeviceFix) Icons.Default.LocationOn else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.locationMode is LocationMode.DeviceFix) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text = if (uiState.locationMode is LocationMode.DeviceFix) {
                                        stringResource(R.string.rule_device_gps_banner)
                                    } else {
                                        stringResource(R.string.rule_device_off_banner)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (uiState.locationMode is LocationMode.DeviceFix) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.locationMode !is LocationMode.DeviceFix) {
                                    TextButton(onClick = {
                                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                    }) {
                                        Text(stringResource(R.string.turn_on_location), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            GrievButton(
                                text = stringResource(R.string.take_photo),
                                onClick = {
                                    val photoFile = File(context.externalCacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                    tempPhotoFile = photoFile
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                type = GrievButtonType.OUTLINED,
                                modifier = Modifier.weight(1f)
                            )
                            GrievButton(
                                text = stringResource(R.string.pick_gallery),
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        mediaLocationPermissionLauncher.launch(Manifest.permission.ACCESS_MEDIA_LOCATION)
                                    } else {
                                        documentPickerLauncher.launch(arrayOf("image/*", "video/*", "application/pdf"))
                                    }
                                },
                                type = GrievButtonType.OUTLINED,
                                modifier = Modifier.weight(1f)
                            )
                            GrievButton(
                                text = stringResource(R.string.attach_pdf),
                                onClick = {
                                    documentPickerLauncher.launch(arrayOf("application/pdf"))
                                },
                                type = GrievButtonType.OUTLINED,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Media Attachment Preview with Geotag Badge & Location Info
                        if (uiState.selectedMediaUri != null) {
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = Shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.sm)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(Shapes.small)
                                            .background(Color.Black.copy(alpha = 0.05f))
                                    ) {
                                        if (uiState.selectedMediaType == MediaType.IMAGE) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = uiState.selectedMediaUri),
                                                contentDescription = "Attachment Preview",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(Spacing.md),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.selectedMediaType == MediaType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.width(Spacing.sm))
                                                Text(
                                                    text = uiState.selectedMediaName ?: "Attached Media Document",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }

                                        // Geotag Status Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = if (uiState.attachmentGeoPoint != null) Color(0xFF2E7D32) else Color(0xFFD84315),
                                            modifier = Modifier.align(Alignment.TopStart).padding(Spacing.xs)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PinDrop,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(Spacing.xxs))
                                                Text(
                                                    text = if (uiState.attachmentGeoPoint != null) stringResource(R.string.badge_geotagged) else stringResource(R.string.badge_no_geotag),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = viewModel::clearMedia,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(Spacing.xs)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Media",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    if (uiState.attachmentGeoPoint != null) {
                                        val gp = uiState.attachmentGeoPoint!!
                                        Spacer(modifier = Modifier.height(Spacing.xs))
                                        Text(
                                            text = String.format(Locale.US, "GPS: %.4f° N, %.4f° E", gp.latitude, gp.longitude),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (uiState.mediaValidationError != null) {
                                        Spacer(modifier = Modifier.height(Spacing.xs))
                                        Text(
                                            text = uiState.mediaValidationError!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 5. Location Section & Mini Map
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = if (uiState.locationSource == "photo_exif") stringResource(R.string.location_from_photo) else stringResource(R.string.location_label),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.locationMode is LocationMode.DeviceFix) {
                                TextButton(onClick = viewModel::fetchLocation) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        if (uiState.isFetchingLocation) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = stringResource(R.string.location_fetching),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (uiState.locationData != null) {
                            val loc = uiState.locationData!!
                            Text(
                                text = "${loc.address} (±${loc.accuracyMeters.toInt()} m)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            val latLng = LatLng(loc.latitude, loc.longitude)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(latLng, 15f)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(Shapes.small)
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    onMapClick = { newLatLng ->
                                        if (uiState.locationMode is LocationMode.DeviceFix) {
                                            viewModel.updateLocationPin(newLatLng.latitude, newLatLng.longitude)
                                        }
                                    }
                                ) {
                                    Marker(
                                        state = MarkerState(position = latLng),
                                        title = "Complaint Location",
                                        snippet = loc.address,
                                        draggable = uiState.locationMode is LocationMode.DeviceFix
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = uiState.locationError ?: stringResource(R.string.location_failed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                if (uiState.submissionState is SubmissionState.Uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                GrievButton(
                    text = stringResource(R.string.submit_btn),
                    onClick = viewModel::submitComplaint,
                    enabled = viewModel.isFormValid(),
                    type = GrievButtonType.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (uiState.isVoiceDictating) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::stopVoiceDictation,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val transition = rememberInfiniteTransition(label = "MicPulse")
                val scale by transition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "MicScale"
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp).scale(scale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = stringResource(R.string.listening_voice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (uiState.voicePartialText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "\"${uiState.voicePartialText}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                TextButton(onClick = viewModel::stopVoiceDictation) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

    // Location Off / No EXIF GPS Dialog
    if (uiState.showLocationOffDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowLocationOffDialog(false) },
            title = { Text(stringResource(R.string.location_off_dialog_title)) },
            text = { Text(stringResource(R.string.location_off_dialog_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setShowLocationOffDialog(false)
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text(stringResource(R.string.turn_on_location))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setShowLocationOffDialog(false)
                    documentPickerLauncher.launch(arrayOf("image/*", "video/*"))
                }) {
                    Text(stringResource(R.string.choose_geotagged_photo))
                }
            }
        )
    }

    if (uiState.showConfirmLeaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowConfirmLeaveDialog(false) },
            title = { Text(stringResource(R.string.confirm_leave_title)) },
            text = { Text(stringResource(R.string.confirm_leave_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setShowConfirmLeaveDialog(false)
                    onBackClick()
                }) {
                    Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowConfirmLeaveDialog(false) }) {
                    Text(stringResource(R.string.keep_editing))
                }
            }
        )
    }
}

@Composable
private fun SubmissionSuccessContent(
    trackingId: String,
    onTrackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.submission_success_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = stringResource(R.string.submission_success_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.your_tracking_id),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = trackingId,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        GrievButton(
                            text = stringResource(R.string.copy_tracking_id),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Tracking ID", trackingId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                            },
                            type = GrievButtonType.OUTLINED,
                            modifier = Modifier.weight(1f)
                        )

                        GrievButton(
                            text = stringResource(R.string.share_tracking_id),
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "My GrievTech complaint ID is $trackingId")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Tracking ID"))
                            },
                            type = GrievButtonType.OUTLINED,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            GrievButton(
                text = stringResource(R.string.track_this_complaint),
                onClick = onTrackClick,
                type = GrievButtonType.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            GrievButton(
                text = stringResource(R.string.back_to_home),
                onClick = onHomeClick,
                type = GrievButtonType.OUTLINED,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitComplaintScreenPreview() {
    GrievTechTheme {
        SubmissionSuccessContent(
            trackingId = "GT-984210",
            onTrackClick = {},
            onHomeClick = {}
        )
    }
}
