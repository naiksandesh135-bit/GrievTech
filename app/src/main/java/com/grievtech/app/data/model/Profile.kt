package com.grievtech.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("id") val id: String = "",
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val role: UserRole = UserRole.CITIZEN,
    @SerialName("preferred_language") val preferredLanguage: String? = "en",
    @SerialName("created_at") val createdAt: String? = null
) {
    val displayName: String
        get() = fullName?.takeIf { it.isNotBlank() } ?: "User"

    val displayPhone: String
        get() = phone?.takeIf { it.isNotBlank() } ?: "N/A"
}
