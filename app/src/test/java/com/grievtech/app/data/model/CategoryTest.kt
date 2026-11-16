package com.grievtech.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryTest {

    private val allowedDbValues = setOf(
        "Road",
        "Water",
        "Electricity",
        "Garbage",
        "Other",
        "Streetlight",
        "Sanitation"
    )

    @Test
    fun category_everyDbValue_isInAllowedList() {
        Category.entries.forEach { category ->
            assertTrue(
                "Category ${category.name} dbValue '${category.dbValue}' is not in allowed list $allowedDbValues",
                allowedDbValues.contains(category.dbValue)
            )
        }
    }

    @Test
    fun category_fromDbValue_parsesAndFallbacksCorrectly() {
        assertEquals(Category.POTHOLES, Category.fromDbValue("Road"))
        assertEquals(Category.STREETLIGHTS, Category.fromDbValue("Streetlight"))
        assertEquals(Category.GARBAGE, Category.fromDbValue("Garbage"))
        assertEquals(Category.WATER, Category.fromDbValue("Water"))
        assertEquals(Category.ELECTRICITY, Category.fromDbValue("Electricity"))
        assertEquals(Category.SANITATION, Category.fromDbValue("Sanitation"))
        assertEquals(Category.OTHER, Category.fromDbValue("Other"))
        assertEquals(Category.OTHER, Category.fromDbValue("UnknownValue"))
    }
}
