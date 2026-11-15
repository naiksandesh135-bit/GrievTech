package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusHistory(
    @SerialName("id") val id: String = "",
    @SerialName("complaint_id") val complaintId: String = "",
    @SerialName("status") val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    @SerialName("note") val note: String? = null,
    @SerialName("changed_by") val changedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
