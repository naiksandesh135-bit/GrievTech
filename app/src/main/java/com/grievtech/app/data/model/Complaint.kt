package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Complaint(
    @SerialName("id") val id: String = "",
    @SerialName("tracking_id") val trackingId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("category") val category: Category = Category.OTHER,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("priority") val priority: Priority = Priority.NORMAL,
    @SerialName("status") val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("media_type") val mediaType: MediaType? = null,
    @SerialName("location_source") val locationSource: String? = "device_gps",
    @SerialName("photo_geotagged") val photoGeotagged: Boolean = false,
    @SerialName("duplicate_of") val duplicateOf: String? = null,
    @SerialName("report_count") val reportCount: Int = 1,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null
)
