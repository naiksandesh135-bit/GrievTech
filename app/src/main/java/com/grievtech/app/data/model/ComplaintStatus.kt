package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ComplaintStatus(val value: String, val label: String) {
    @SerialName("Submitted")
    SUBMITTED("Submitted", "Submitted"),

    @SerialName("Assigned")
    ASSIGNED("Assigned", "Assigned"),

    @SerialName("In Progress")
    IN_PROGRESS("In Progress", "In Progress"),

    @SerialName("Resolved")
    RESOLVED("Resolved", "Resolved"),

    @SerialName("Closed")
    CLOSED("Closed", "Closed")
}
