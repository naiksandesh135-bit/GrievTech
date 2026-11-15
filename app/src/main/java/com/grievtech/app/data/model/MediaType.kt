package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaType(val value: String, val label: String) {
    @SerialName("image")
    IMAGE("image", "Image"),

    @SerialName("video")
    VIDEO("video", "Video"),

    @SerialName("pdf")
    PDF("pdf", "PDF Document")
}
