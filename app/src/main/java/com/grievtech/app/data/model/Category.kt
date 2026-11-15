package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Category(val value: String, val label: String) {
    @SerialName("Potholes")
    POTHOLES("Potholes", "Potholes & Roads"),

    @SerialName("Streetlights")
    STREETLIGHTS("Streetlights", "Broken Streetlight"),

    @SerialName("Garbage")
    GARBAGE("Garbage", "Garbage & Sanitation"),

    @SerialName("Water")
    WATER("Water", "Water Supply & Leakage"),

    @SerialName("Electricity")
    ELECTRICITY("Electricity", "Electricity Outage"),

    @SerialName("Other")
    OTHER("Other", "Other Civic Grievance")
}
