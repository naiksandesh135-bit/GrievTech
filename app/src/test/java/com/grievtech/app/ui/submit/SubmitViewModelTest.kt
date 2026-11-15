package com.grievtech.app.ui.submit

import android.content.ContextWrapper
import androidx.lifecycle.SavedStateHandle
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.Priority
import com.grievtech.app.data.model.TrackResult
import com.grievtech.app.data.repository.ComplaintRepository
import com.grievtech.app.util.LocationFetchResult
import com.grievtech.app.util.LocationProvider
import com.grievtech.app.util.MediaProcessResult
import com.grievtech.app.util.MediaProcessor
import com.grievtech.app.util.Resource
import com.grievtech.app.util.UserLocationData
import com.grievtech.app.util.VoiceInputManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeComplaintRepository: FakeComplaintRepository
    private lateinit var fakeLocationProvider: FakeLocationProvider
    private lateinit var fakeMediaProcessor: FakeMediaProcessor
    private lateinit var fakeVoiceInputManager: FakeVoiceInputManager
    private lateinit var viewModel: SubmitViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeComplaintRepository = FakeComplaintRepository()
        fakeLocationProvider = FakeLocationProvider()
        fakeMediaProcessor = FakeMediaProcessor()
        fakeVoiceInputManager = FakeVoiceInputManager()

        viewModel = SubmitViewModel(
            complaintRepository = fakeComplaintRepository,
            locationProvider = fakeLocationProvider,
            mediaProcessor = fakeMediaProcessor,
            voiceInputManager = fakeVoiceInputManager,
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onTitleChanged_updatesTitleAndLimitsTo80Chars() {
        val longTitle = "A".repeat(100)
        viewModel.onTitleChanged(longTitle)

        assertEquals(80, viewModel.uiState.value.title.length)
        assertEquals(null, viewModel.uiState.value.titleError)
    }

    @Test
    fun onDescriptionChanged_shortDescription_setsError() {
        viewModel.onDescriptionChanged("Short desc")

        assertEquals("Description must be at least 20 characters", viewModel.uiState.value.descriptionError)
    }

    @Test
    fun submitComplaint_validForm_submitsSuccessfully() = runTest {
        viewModel.onTitleChanged("Dangerous pothole on main road")
        viewModel.onDescriptionChanged("The pothole is deep and causing severe traffic risk near the junction.")
        viewModel.onCategoryChanged(Category.POTHOLES)
        viewModel.onPriorityChanged(Priority.EMERGENCY)

        viewModel.updateLocationPin(12.9716, 77.5946)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submitComplaint()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.submissionState
        assert(state is SubmissionState.Success)
        assertEquals("GT-984210", (state as SubmissionState.Success).trackingId)
    }
}

class FakeComplaintRepository : ComplaintRepository {
    override fun getComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Success(emptyList()))
    }

    override fun createComplaint(
        complaint: Complaint,
        media: MediaProcessResult?
    ): Flow<Resource<Complaint>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(complaint.copy(id = "comp_123", trackingId = "GT-984210")))
    }

    override suspend fun checkDuplicates(complaint: Complaint): List<Complaint> {
        return emptyList()
    }

    override suspend fun trackByTrackingId(trackingId: String): Resource<TrackResult> {
        return Resource.Success(TrackResult(trackingId = trackingId))
    }
}

class FakeLocationProvider : LocationProvider(ContextWrapper(null)) {
    override suspend fun getCurrentLocation(): LocationFetchResult {
        return LocationFetchResult.Success(
            UserLocationData(12.9716, 77.5946, 5f, "MG Road, Bengaluru")
        )
    }

    override suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return "MG Road, Bengaluru"
    }
}

class FakeMediaProcessor : MediaProcessor(ContextWrapper(null))

class FakeVoiceInputManager : VoiceInputManager(ContextWrapper(null))
