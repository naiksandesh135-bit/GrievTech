package com.grievtech.app.data.model

import androidx.annotation.StringRes
import com.grievtech.app.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CategorySerializer::class)
enum class Category(
    val dbValue: String,
    @StringRes val labelRes: Int
) {
    POTHOLES("Road", R.string.issue_potholes_title),
    STREETLIGHTS("Streetlight", R.string.issue_streetlights_title),
    GARBAGE("Garbage", R.string.issue_garbage_title),
    WATER("Water", R.string.issue_water_title),
    ELECTRICITY("Electricity", R.string.issue_electricity_title),
    SANITATION("Sanitation", R.string.issue_garbage_title),
    OTHER("Other", R.string.issue_other_title);

    companion object {
        fun fromDbValue(value: String?): Category {
            return when (value?.trim()?.lowercase()) {
                "road", "potholes", "potholes & roads" -> POTHOLES
                "streetlight", "streetlights", "broken streetlight" -> STREETLIGHTS
                "garbage", "garbage & sanitation" -> GARBAGE
                "water", "water supply", "water supply & leakage" -> WATER
                "electricity", "electricity outage" -> ELECTRICITY
                "sanitation" -> SANITATION
                else -> OTHER
            }
        }
    }
}

object CategorySerializer : KSerializer<Category> {
    override val descriptor = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Category) {
        encoder.encodeString(value.dbValue)
    }

    override fun deserialize(decoder: Decoder): Category {
        val stringValue = decoder.decodeString()
        return Category.fromDbValue(stringValue)
    }
}
