package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackResult(
    @SerialName("tracking_id") val trackingId: String = "",
    @SerialName("category") val category: Category = Category.OTHER,
    @SerialName("title") val title: String = "",
    @SerialName("status") val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    @SerialName("priority") val priority: Priority = Priority.NORMAL,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)
