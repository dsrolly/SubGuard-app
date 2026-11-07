package com.davinder.subguard.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUtilsTest {

    private val dob2000 = parseDobIso("2000-01-01")!!

    private fun validForm() = ProfileFormState(
        name = "Davinder", countryCode = "+91", phone = "9876543210", dobMillis = dob2000, gender = "Male"
    )

    @Test
    fun validFormPasses() {
        assertNull(validateProfileForm(validForm(), requireName = true))
    }

    @Test
    fun indianNumberMustBeTenDigits() {
        assertEquals(
            "Enter a valid 10-digit mobile number",
            validateProfileForm(validForm().copy(phone = "98765"), requireName = true)
        )
    }

    @Test
    fun indianMobileMustStartWithSixToNine() {
        assertEquals(
            "Enter a valid 10-digit mobile number",
            validateProfileForm(validForm().copy(phone = "1234567890"), requireName = true)
        )
    }

    @Test
    fun nameRequiredOnlyWhenAsked() {
        val noName = validForm().copy(name = "")
        assertNull(validateProfileForm(noName, requireName = false))
        assertNotNull(validateProfileForm(noName, requireName = true))
    }

    @Test
    fun futureDobRejected() {
        val tomorrow = System.currentTimeMillis() + 24L * 60 * 60 * 1000
        assertEquals(
            "Date of birth can't be in the future",
            validateProfileForm(validForm().copy(dobMillis = tomorrow), requireName = true)
        )
    }

    @Test
    fun underageRejected() {
        val fiveYearsAgo = System.currentTimeMillis() - 5L * 365 * 24 * 60 * 60 * 1000
        val error = validateProfileForm(validForm().copy(dobMillis = fiveYearsAgo), requireName = true)
        assertTrue(error!!.contains("at least"))
    }

    @Test
    fun ageCountsBirthdayCorrectly() {
        val dob = parseDobIso("2000-06-15")!!
        assertEquals(23, ageFromDobMillis(dob, parseDobIso("2024-06-14")!!))
        assertEquals(24, ageFromDobMillis(dob, parseDobIso("2024-06-15")!!))
    }

    @Test
    fun parsePhoneHandlesAllShapes() {
        assertEquals("+91" to "9876543210", parsePhone("+91 9876543210"))
        assertEquals("+91" to "9876543210", parsePhone("+919876543210"))
        assertEquals("+91" to "", parsePhone(""))
        assertEquals("+1" to "5551234567", parsePhone("+1 5551234567"))
    }

    @Test
    fun phoneStoredFormat() {
        assertEquals("+91 9876543210", validForm().phoneStored)
        assertEquals("", validForm().copy(phone = "").phoneStored)
    }

    @Test
    fun completionFullAndPartial() {
        assertEquals(100, calculateProfileCompletion("Dev", "+91 9876543210", "2000-01-01", "Male", true).percent)
        val partial = calculateProfileCompletion("User", "", "2000-01-01", "Male", true)
        assertEquals(60, partial.percent)
        assertEquals(listOf("Name", "Phone number"), partial.missing)
    }
}