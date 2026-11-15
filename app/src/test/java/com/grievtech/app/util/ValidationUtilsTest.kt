package com.grievtech.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun isValidEmail_validEmails_returnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("citizen@grievtech.org"))
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.in"))
    }

    @Test
    fun isValidEmail_invalidEmails_returnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("not-an-email"))
        assertFalse(ValidationUtils.isValidEmail(null))
    }

    @Test
    fun isValidPhone_validPhone_returnsTrue() {
        assertTrue(ValidationUtils.isValidPhone("+919876543210"))
        assertTrue(ValidationUtils.isValidPhone("9876543210"))
    }

    @Test
    fun isValidPhone_invalidPhone_returnsFalse() {
        assertFalse(ValidationUtils.isValidPhone("123"))
        assertFalse(ValidationUtils.isValidPhone(null))
    }

    @Test
    fun validateFullName_validName_returnsTrue() {
        assertTrue(ValidationUtils.validateFullName("John Doe"))
        assertTrue(ValidationUtils.validateFullName("  Ravi  "))
    }

    @Test
    fun validateFullName_invalidName_returnsFalse() {
        assertFalse(ValidationUtils.validateFullName("A"))
        assertFalse(ValidationUtils.validateFullName(""))
        assertFalse(ValidationUtils.validateFullName(null))
    }

    @Test
    fun validateConfirmPassword_matching_returnsTrue() {
        assertTrue(ValidationUtils.validateConfirmPassword("Password@123", "Password@123"))
    }

    @Test
    fun validateConfirmPassword_mismatch_returnsFalse() {
        assertFalse(ValidationUtils.validateConfirmPassword("Password@123", "DifferentPass"))
        assertFalse(ValidationUtils.validateConfirmPassword("", ""))
    }

    @Test
    fun checkPasswordStrength_evaluatesCorrectly() {
        assertEquals(ValidationUtils.PasswordStrength.WEAK, ValidationUtils.checkPasswordStrength("123"))
        assertEquals(ValidationUtils.PasswordStrength.MEDIUM, ValidationUtils.checkPasswordStrength("pass123"))
        assertEquals(ValidationUtils.PasswordStrength.STRONG, ValidationUtils.checkPasswordStrength("StrongPass@123"))
    }
}
