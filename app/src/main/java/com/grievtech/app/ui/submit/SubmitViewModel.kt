package com.grievtech.app.ui.submit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.MediaType
import com.grievtech.app.data.model.Priority
import com.grievtech.app.data.repository.ComplaintRepository
import com.grievtech.app.util.GeoPoint
import com.grievtech.app.util.GeoTagReader
import com.grievtech.app.util.LocationFetchResult
import com.grievtech.app.util.LocationMode
import com.grievtech.app.util.LocationProvider
import com.grievtech.app.util.MediaProcessResult
import com.grievtech.app.util.MediaProcessor
import com.grievtech.app.util.Resource
import com.grievtech.app.util.SpeechState
import com.grievtech.app.util.UserLocationData
import com.grievtech.app.util.VoiceInputManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Uploading : SubmissionState()
    data class Success(val trackingId: String, val createdComplaint: Complaint) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

data class SubmitUiState(
    val category: Category = Category.POTHOLES,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val priority: Priority = Priority.NORMAL,
    val mediaResult: MediaProcessResult? = null,
    val selectedMediaUri: Uri? = null,
    val selectedMediaName: String? = null,
    val selectedMediaType: MediaType? = null,
    val attachmentGeoPoint: GeoPoint? = null,
    val locationMode: LocationMode = LocationMode.DeviceOff,
    val locationData: UserLocationData? = null,
    val locationSource: String = "device_gps",
    val photoGeotagged: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val locationError: String? = null,
    val mediaValidationError: String? = null,
    val showLocationOffDialog: Boolean = false,
    val submissionState: SubmissionState = SubmissionState.Idle,
    val isVoiceDictating: Boolean = false,
    val voicePartialText: String = "",
    val isDirty: Boolean = false,
    val showConfirmLeaveDialog: Boolean = false
)

@HiltViewModel
class SubmitViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val locationProvider: LocationProvider,
    private val mediaProcessor: MediaProcessor,
    private val voiceInputManager: VoiceInputManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SubmitUiState(
            category = Category.valueOf(savedStateHandle["category"] ?: Category.POTHOLES.name),
            title = savedStateHandle["title"] ?: "",
            description = savedStateHandle["description"] ?: "",
            priority = Priority.valueOf(savedStateHandle["priority"] ?: Priority.NORMAL.name)
        )
    )
    val uiState: StateFlow<SubmitUiState> = _uiState.asStateFlow()

    val speechState: StateFlow<SpeechState> = voiceInputManager.speechState

    init {
        observeSpeechState()
        observeLocationProviderState()
        fetchLocation()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            voiceInputManager.speechState.collectLatest { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _uiState.update { it.copy(isVoiceDictating = true, voicePartialText = "") }
                    }
                    is SpeechState.PartialResult -> {
                        _uiState.update { it.copy(isVoiceDictating = true, voicePartialText = state.text) }
                    }
                    is SpeechState.FinalResult -> {
                        val currentDesc = _uiState.value.description
                        val newDesc = if (currentDesc.isBlank()) state.text else "$currentDesc ${state.text}"
                        onDescriptionChanged(newDesc.take(500))
                        _uiState.update { it.copy(isVoiceDictating = false, voicePartialText = "") }
                    }
                    is SpeechState.Error -> {
                        _uiState.update { it.copy(isVoiceDictating = false, voicePartialText = "") }
                    }
                    is SpeechState.Idle -> {
                        _uiState.update { it.copy(isVoiceDictating = false) }
                    }
                }
            }
        }
    }

    private fun observeLocationProviderState() {
        viewModelScope.launch {
            locationProvider.observeLocationState().collectLatest { isEnabled ->
                Log.d("SubmitViewModel", "Location state changed: isEnabled=$isEnabled")
                if (isEnabled) {
                    fetchLocation()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            locationMode = LocationMode.DeviceOff,
                            isFetchingLocation = false
                        )
                    }
                    reevaluateEvidenceAndLocation()
                }
            }
        }
    }

    fun onCategoryChanged(category: Category) {
        savedStateHandle["category"] = category.name
        _uiState.update { it.copy(category = category, isDirty = true) }
    }

    fun onTitleChanged(title: String) {
        val trimmed = title.take(80)
        savedStateHandle["title"] = trimmed
        _uiState.update {
            it.copy(
                title = trimmed,
                titleError = if (trimmed.isBlank()) "Title is required" else null,
                isDirty = true
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        val trimmed = description.take(500)
        savedStateHandle["description"] = trimmed
        val error = when {
            trimmed.isBlank() -> "Description is required"
            trimmed.length < 20 -> "Description must be at least 20 characters"
            else -> null
        }
        _uiState.update {
            it.copy(
                description = trimmed,
                descriptionError = error,
                isDirty = true
            )
        }
    }

    fun onPriorityChanged(priority: Priority) {
        savedStateHandle["priority"] = priority.name
        _uiState.update { it.copy(priority = priority, isDirty = true) }
    }

    fun onMediaSelected(uri: Uri) {
        viewModelScope.launch {
            when (val processResult = mediaProcessor.processMediaUri(uri)) {
                is MediaProcessResult.Success -> {
                    val geoPoint = processResult.geoPoint
                    _uiState.update { state ->
                        state.copy(
                            mediaResult = processResult,
                            selectedMediaUri = uri,
                            selectedMediaName = processResult.fileName,
                            selectedMediaType = processResult.mediaType,
                            attachmentGeoPoint = geoPoint,
                            isDirty = true
                        )
                    }
                    reevaluateEvidenceAndLocation()
                }
                is MediaProcessResult.Error -> {
                    _uiState.update {
                        it.copy(submissionState = SubmissionState.Error(processResult.message))
                    }
                }
            }
        }
    }

    fun onCameraPhotoCaptured(filePath: String, uri: Uri) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.locationMode is LocationMode.DeviceFix) {
                val loc = (state.locationMode as LocationMode.DeviceFix).locationData
                GeoTagReader.embedGpsIntoExif(filePath, loc.latitude, loc.longitude)
            }
            onMediaSelected(uri)
        }
    }

    fun clearMedia() {
        _uiState.update {
            it.copy(
                mediaResult = null,
                selectedMediaUri = null,
                selectedMediaName = null,
                selectedMediaType = null,
                attachmentGeoPoint = null,
                mediaValidationError = null
            )
        }
        reevaluateEvidenceAndLocation()
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true, locationError = null) }
            if (!locationProvider.isLocationEnabled()) {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        locationMode = LocationMode.DeviceOff,
                        locationError = "Location is turned off."
                    )
                }
                reevaluateEvidenceAndLocation()
                return@launch
            }

            when (val result = locationProvider.getCurrentLocation()) {
                is LocationFetchResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isFetchingLocation = false,
                            locationMode = LocationMode.DeviceFix(result.locationData),
                            locationData = result.locationData,
                            locationSource = "device_gps",
                            locationError = null
                        )
                    }
                    reevaluateEvidenceAndLocation()
                }
                is LocationFetchResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isFetchingLocation = false,
                            locationMode = LocationMode.DeviceOnNoFix,
                            locationError = result.message
                        )
                    }
                    reevaluateEvidenceAndLocation()
                }
            }
        }
    }

    fun updateLocationPin(latitude: Double, longitude: Double) {
        if (_uiState.value.locationMode !is LocationMode.DeviceFix) return
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true) }
            val address = locationProvider.getAddressFromCoordinates(latitude, longitude)
            val updatedData = UserLocationData(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = 5f,
                address = address
            )
            _uiState.update {
                it.copy(
                    isFetchingLocation = false,
                    locationMode = LocationMode.DeviceFix(updatedData),
                    locationData = updatedData,
                    locationSource = "device_gps",
                    locationError = null,
                    isDirty = true
                )
            }
        }
    }

    fun reevaluateEvidenceAndLocation() {
        val state = _uiState.value
        val mode = state.locationMode
        val geoPoint = state.attachmentGeoPoint
        val hasMedia = state.selectedMediaUri != null
        val isPdf = state.selectedMediaType == MediaType.PDF

        when (mode) {
            is LocationMode.DeviceFix -> {
                _uiState.update {
                    it.copy(
                        locationData = mode.locationData,
                        locationSource = "device_gps",
                        photoGeotagged = geoPoint != null && geoPoint.isValid(),
                        mediaValidationError = if (!hasMedia) "Please attach photo or video evidence" else null,
                        showLocationOffDialog = false
                    )
                }
            }
            is LocationMode.DeviceOff, is LocationMode.DeviceOnNoFix -> {
                if (!hasMedia || isPdf) {
                    _uiState.update {
                        it.copy(
                            locationData = null,
                            locationSource = "photo_exif",
                            photoGeotagged = false,
                            mediaValidationError = "A geotagged photo or video is required when location is off.",
                            showLocationOffDialog = false
                        )
                    }
                } else if (geoPoint != null && geoPoint.isValid()) {
                    viewModelScope.launch {
                        val address = locationProvider.getAddressFromCoordinates(geoPoint.latitude, geoPoint.longitude)
                        val exifLocationData = UserLocationData(
                            latitude = geoPoint.latitude,
                            longitude = geoPoint.longitude,
                            accuracyMeters = 10f,
                            address = address
                        )
                        _uiState.update {
                            it.copy(
                                locationData = exifLocationData,
                                locationSource = "photo_exif",
                                photoGeotagged = true,
                                mediaValidationError = null,
                                showLocationOffDialog = false
                            )
                        }
                    }
                } else {
                    // Media attached but no EXIF GPS
                    _uiState.update {
                        it.copy(
                            locationData = null,
                            locationSource = "photo_exif",
                            photoGeotagged = false,
                            mediaValidationError = "Location is off and this photo has no location data. Turn on location, or use a geotagged photo.",
                            showLocationOffDialog = true
                        )
                    }
                }
            }
        }
    }

    fun startVoiceDictation(languageCode: String) {
        voiceInputManager.startListening(languageCode)
    }

    fun stopVoiceDictation() {
        voiceInputManager.stopListening()
    }

    fun setShowLocationOffDialog(show: Boolean) {
        _uiState.update { it.copy(showLocationOffDialog = show) }
    }

    fun setShowConfirmLeaveDialog(show: Boolean) {
        _uiState.update { it.copy(showConfirmLeaveDialog = show) }
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        val isTitleValid = state.title.isNotBlank()
        val isDescValid = state.description.length >= 20
        val isLocationValid = state.locationData != null
        val isMediaValid = state.selectedMediaUri != null && state.mediaValidationError == null
        val notUploading = state.submissionState !is SubmissionState.Uploading

        return isTitleValid && isDescValid && isLocationValid && isMediaValid && notUploading
    }

    fun submitComplaint() {
        if (!isFormValid()) return
        val state = _uiState.value

        val complaint = Complaint(
            category = state.category,
            title = state.title.trim(),
            description = state.description.trim(),
            priority = state.priority,
            latitude = state.locationData?.latitude,
            longitude = state.locationData?.longitude,
            address = state.locationData?.address,
            locationSource = state.locationSource,
            photoGeotagged = state.photoGeotagged
        )

        viewModelScope.launch {
            complaintRepository.createComplaint(complaint, state.mediaResult).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(submissionState = SubmissionState.Uploading) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                submissionState = SubmissionState.Success(
                                    trackingId = result.data.trackingId,
                                    createdComplaint = result.data
                                ),
                                isDirty = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(submissionState = SubmissionState.Error(result.message))
                        }
                    }
                }
            }
        }
    }
}
