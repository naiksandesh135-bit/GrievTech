package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Priority(val value: String, val label: String) {
    @SerialName("Normal")
    NORMAL("Normal", "Normal Priority"),

    @SerialName("Emergency")
    EMERGENCY("Emergency", "Emergency Priority")
}
