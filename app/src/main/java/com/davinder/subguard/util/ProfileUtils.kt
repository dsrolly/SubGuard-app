package com.davinder.subguard.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val MIN_AGE_YEARS = 13
const val DEFAULT_COUNTRY_CODE = "+91"
/** Placeholder name RegisterScreen uses when the user leaves Name blank. */
const val DEFAULT_DISPLAY_NAME = "User"
val GENDER_OPTIONS = listOf("Male", "Female", "Other", "Prefer not to say")

/** Editable profile form, shared by ProfileSetup and EditProfile. */
data class ProfileFormState(
    val name: String = "",
    val countryCode: String = DEFAULT_COUNTRY_CODE,
    val phone: String = "",
    val dobMillis: Long? = null,   // DatePicker millis (UTC midnight)
    val gender: String? = null
) {
    /** Stored form: "+91 9876543210". Blank if no number entered. */
    val phoneStored: String
        get() = if (phone.isBlank()) "" else "${countryCode.trim()} ${phone.trim()}"
}

/** "+91 9876543210" -> ("+91", "9876543210"). Handles old values without a space. */
fun parsePhone(stored: String): Pair<String, String> {
    val s = stored.trim()
    if (s.isEmpty()) return DEFAULT_COUNTRY_CODE to ""
    if (' ' in s) return s.substringBefore(' ') to s.substringAfter(' ').filter { it.isDigit() }
    if (s.startsWith("+91") && s.length > 3) return "+91" to s.substring(3).filter { it.isDigit() }
    return DEFAULT_COUNTRY_CODE to s.filter { it.isDigit() }
}

private fun utcFormat(pattern: String, locale: Locale) =
    SimpleDateFormat(pattern, locale).apply { timeZone = TimeZone.getTimeZone("UTC") }

/** "05 Mar 2002". DatePicker millis are UTC, so format in UTC. */
fun formatDobForDisplay(millis: Long): String =
    utcFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatDobIso(millis: Long): String = utcFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

fun parseDobIso(iso: String): Long? = try {
    utcFormat("yyyy-MM-dd", Locale.US).parse(iso)?.time
} catch (e: Exception) {
    null
}

fun ageFromDobMillis(dobMillis: Long, nowMillis: Long = System.currentTimeMillis()): Int {
    val utc = TimeZone.getTimeZone("UTC")
    val dob = Calendar.getInstance(utc).apply { timeInMillis = dobMillis }
    val now = Calendar.getInstance(utc).apply { timeInMillis = nowMillis }
    var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    val beforeBirthday = now.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
            (now.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
                    now.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))
    if (beforeBirthday) age--
    return age
}

/** Returns an error message, or null if the form is valid. */
fun validateProfileForm(
    form: ProfileFormState,
    requireName: Boolean,
    nowMillis: Long = System.currentTimeMillis()
): String? {
    if (requireName && form.name.trim().length < 2) return "Enter your name (at least 2 characters)"

    val code = form.countryCode.filter { it.isDigit() }
    val number = form.phone.filter { it.isDigit() }
    if (code.isEmpty() || code.length > 4) return "Enter a valid country code"
    if (code == "91" && (number.length != 10 || number[0] !in '6'..'9')) {
        return "Enter a valid 10-digit mobile number"
    }
    if (number.length !in 6..14 || code.length + number.length > 15) return "Enter a valid phone number"

    val dob = form.dobMillis ?: return "Select your date of birth"
    if (dob > nowMillis) return "Date of birth can't be in the future"
    if (ageFromDobMillis(dob, nowMillis) < MIN_AGE_YEARS) {
        return "You must be at least $MIN_AGE_YEARS years old to use SubGuard"
    }
    if (form.gender == null) return "Select a gender option"
    return null
}

data class ProfileCompletion(val percent: Int, val missing: List<String>)

/** 5 equal items: name, phone, date of birth, gender, verified email. */
fun calculateProfileCompletion(
    displayName: String,
    phone: String,
    dob: String,
    gender: String,
    emailVerified: Boolean
): ProfileCompletion {
    val missing = buildList {
        if (displayName.isBlank() || displayName == DEFAULT_DISPLAY_NAME) add("Name")
        if (phone.isBlank()) add("Phone number")
        if (dob.isBlank()) add("Date of birth")
        if (gender.isBlank()) add("Gender")
        if (!emailVerified) add("Verified email")
    }
    val total = 5
    return ProfileCompletion(percent = (total - missing.size) * 100 / total, missing = missing)
}