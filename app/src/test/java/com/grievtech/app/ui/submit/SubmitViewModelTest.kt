package com.grievtech.app.ui.submit

import android.content.ContextWrapper
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.MediaType
import com.grievtech.app.data.model.TrackResult
import com.grievtech.app.data.repository.ComplaintRepository
import com.grievtech.app.util.GeoPoint
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

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
    fun locationMode_deviceFixWithMedia_valid() = runTest {
        viewModel.onTitleChanged("Pothole issue")
        viewModel.onDescriptionChanged("The pothole is deep and causing severe traffic risk near the junction.")
        viewModel.fetchLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mock(Uri::class.java)
        fakeMediaProcessor.stubResult = MediaProcessResult.Success(
            bytes = byteArrayOf(1, 2, 3),
            fileName = "img_123.jpg",
            mimeType = "image/jpeg",
            mediaType = MediaType.IMAGE,
            geoPoint = null
        )
        viewModel.onMediaSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isFormValid())
        assertEquals("device_gps", viewModel.uiState.value.locationSource)
    }

    @Test
    fun locationMode_deviceFixNoMedia_invalid() = runTest {
        viewModel.onTitleChanged("Pothole issue")
        viewModel.onDescriptionChanged("The pothole is deep and causing severe traffic risk near the junction.")
        viewModel.fetchLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun locationMode_locationOffWithGeotaggedMedia_validAndUsesExifCoordinates() = runTest {
        fakeLocationProvider.isLocationEnabledValue = false
        viewModel.onTitleChanged("Pothole issue")
        viewModel.onDescriptionChanged("The pothole is deep and causing severe traffic risk near the junction.")
        viewModel.fetchLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mock(Uri::class.java)
        fakeMediaProcessor.stubResult = MediaProcessResult.Success(
            bytes = byteArrayOf(1, 2, 3),
            fileName = "img_123.jpg",
            mimeType = "image/jpeg",
            mediaType = MediaType.IMAGE,
            geoPoint = GeoPoint(12.9716, 77.5946)
        )
        viewModel.onMediaSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isFormValid())
        assertEquals("photo_exif", viewModel.uiState.value.locationSource)
        assertTrue(viewModel.uiState.value.photoGeotagged)
        assertNotNull(viewModel.uiState.value.locationData)
        assertEquals(12.9716, viewModel.uiState.value.locationData!!.latitude, 0.0001)
    }

    @Test
    fun locationMode_locationOffWithNonGeotaggedMedia_invalid() = runTest {
        fakeLocationProvider.isLocationEnabledValue = false
        viewModel.onTitleChanged("Pothole issue")
        viewModel.onDescriptionChanged("The pothole is deep and causing severe traffic risk near the junction.")
        viewModel.fetchLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mock(Uri::class.java)
        fakeMediaProcessor.stubResult = MediaProcessResult.Success(
            bytes = byteArrayOf(1, 2, 3),
            fileName = "img_123.jpg",
            mimeType = "image/jpeg",
            mediaType = MediaType.IMAGE,
            geoPoint = null
        )
        viewModel.onMediaSelected(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isFormValid())
        assertTrue(viewModel.uiState.value.showLocationOffDialog)
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
    var isLocationEnabledValue = true

    override fun isLocationEnabled(): Boolean = isLocationEnabledValue

    override suspend fun getCurrentLocation(): LocationFetchResult {
        return if (isLocationEnabledValue) {
            LocationFetchResult.Success(
                UserLocationData(12.9716, 77.5946, 5f, "MG Road, Bengaluru")
            )
        } else {
            LocationFetchResult.Error("Location is turned off.")
        }
    }

    override suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return "MG Road, Bengaluru"
    }
}

class FakeMediaProcessor : MediaProcessor(ContextWrapper(null)) {
    var stubResult: MediaProcessResult = MediaProcessResult.Success(
        bytes = byteArrayOf(1, 2, 3),
        fileName = "test.jpg",
        mimeType = "image/jpeg",
        mediaType = MediaType.IMAGE
    )

    override fun processMediaUri(uri: Uri): MediaProcessResult {
        return stubResult
    }
}

class FakeVoiceInputManager : VoiceInputManager(ContextWrapper(null))
