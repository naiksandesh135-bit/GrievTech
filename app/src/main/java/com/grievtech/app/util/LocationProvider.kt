package com.grievtech.app.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class UserLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val address: String
)

sealed class LocationFetchResult {
    data class Success(val locationData: UserLocationData) : LocationFetchResult()
    data class Error(val message: String) : LocationFetchResult()
}

sealed class LocationMode {
    data class DeviceFix(val locationData: UserLocationData) : LocationMode()
    object DeviceOnNoFix : LocationMode()
    object DeviceOff : LocationMode()
}

@Singleton
open class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    open fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    open fun observeLocationState(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isLocationEnabled())
            }
        }
        val filter = IntentFilter().apply {
            addAction(LocationManager.MODE_CHANGED_ACTION)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
        trySend(isLocationEnabled())
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    open suspend fun getCurrentLocation(): LocationFetchResult = withContext(Dispatchers.IO) {
        if (!isLocationEnabled()) {
            return@withContext LocationFetchResult.Error("Location is turned off.")
        }
        try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = withTimeoutOrNull(15_000L) {
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnSuccessListener { loc ->
                        if (continuation.isActive) continuation.resume(loc)
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            } ?: run {
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        if (continuation.isActive) continuation.resume(loc)
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }

            if (location != null) {
                val address = getAddressFromCoordinates(location.latitude, location.longitude)
                val data = UserLocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy,
                    address = address
                )
                LocationFetchResult.Success(data)
            } else {
                LocationFetchResult.Error("Unable to capture GPS fix.")
            }
        } catch (e: Exception) {
            LocationFetchResult.Error(e.localizedMessage ?: "Failed to fetch GPS location.")
        }
    }

    open suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        val fallbackAddress = String.format(Locale.US, "%.4f° N, %.4f° E", latitude, longitude)
        try {
            if (!Geocoder.isPresent()) return@withContext fallbackAddress
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val addr = addresses.firstOrNull()?.formatToString() ?: fallbackAddress
                            if (continuation.isActive) continuation.resume(addr)
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(fallbackAddress)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.formatToString() ?: fallbackAddress
            }
        } catch (e: Exception) {
            fallbackAddress
        }
    }

    private fun Address.formatToString(): String {
        val sb = StringBuilder()
        val feature = featureName ?: thoroughfare
        if (!feature.isNullOrBlank()) sb.append(feature).append(", ")
        if (!subLocality.isNullOrBlank()) sb.append(subLocality).append(", ")
        if (!locality.isNullOrBlank()) sb.append(locality)
        return sb.toString().ifBlank {
            String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
    }

    private fun String?.isNullOrBlank(): Boolean = this == null || this.trim().isEmpty()
}
