package com.grievtech.app.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserRoleSerializer::class)
enum class UserRole(val value: String, val label: String) {
    @SerialName("citizen")
    CITIZEN("citizen", "Citizen"),

    @SerialName("admin")
    ADMIN("admin", "Administrator");

    companion object {
        fun fromValue(value: String?): UserRole {
            return when (value?.lowercase()?.trim()) {
                "admin", "administrator" -> ADMIN
                else -> CITIZEN
            }
        }
    }
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor = PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): UserRole {
        val stringValue = decoder.decodeString()
        return UserRole.fromValue(stringValue)
    }
}
