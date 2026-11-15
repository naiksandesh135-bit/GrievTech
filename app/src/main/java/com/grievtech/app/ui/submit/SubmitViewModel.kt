package com.grievtech.app.ui.submit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.MediaType
import com.grievtech.app.data.model.Priority
import com.grievtech.app.data.repository.ComplaintRepository
import com.grievtech.app.util.LocationFetchResult
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
    val locationData: UserLocationData? = null,
    val isFetchingLocation: Boolean = false,
    val locationError: String? = null,
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
                    _uiState.update {
                        it.copy(
                            mediaResult = processResult,
                            selectedMediaUri = uri,
                            selectedMediaName = processResult.fileName,
                            selectedMediaType = processResult.mediaType,
                            isDirty = true
                        )
                    }
                }
                is MediaProcessResult.Error -> {
                    _uiState.update {
                        it.copy(submissionState = SubmissionState.Error(processResult.message))
                    }
                }
            }
        }
    }

    fun clearMedia() {
        _uiState.update {
            it.copy(
                mediaResult = null,
                selectedMediaUri = null,
                selectedMediaName = null,
                selectedMediaType = null
            )
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true, locationError = null) }
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationFetchResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isFetchingLocation = false,
                            locationData = result.locationData,
                            locationError = null
                        )
                    }
                }
                is LocationFetchResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isFetchingLocation = false,
                            locationError = result.message
                        )
                    }
                }
            }
        }
    }

    fun updateLocationPin(latitude: Double, longitude: Double) {
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
                    locationData = updatedData,
                    locationError = null,
                    isDirty = true
                )
            }
        }
    }

    fun startVoiceDictation(languageCode: String) {
        voiceInputManager.startListening(languageCode)
    }

    fun stopVoiceDictation() {
        voiceInputManager.stopListening()
    }

    fun setShowConfirmLeaveDialog(show: Boolean) {
        _uiState.update { it.copy(showConfirmLeaveDialog = show) }
    }

    fun submitComplaint() {
        val state = _uiState.value
        var isValid = true

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            isValid = false
        }
        if (state.description.isBlank() || state.description.length < 20) {
            _uiState.update { it.copy(descriptionError = "Description must be at least 20 characters") }
            isValid = false
        }
        if (state.locationData == null) {
            _uiState.update { it.copy(locationError = "GPS Location is required to submit a grievance.") }
            isValid = false
        }

        if (!isValid) return

        val complaint = Complaint(
            category = state.category,
            title = state.title.trim(),
            description = state.description.trim(),
            priority = state.priority,
            latitude = state.locationData?.latitude,
            longitude = state.locationData?.longitude,
            address = state.locationData?.address
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
