package com.example.chat.olinam

/**
 * Production-grade Phone Number Normalization for Olinam.
 * Standardizes all phone variations (local, international, with spaces/dashes)
 * to canonical E.164 and 10-digit national identifiers for collision-free routing.
 */
object PhoneNormalizer {

    private const val DEFAULT_COUNTRY_CODE = "91" // India default (+91)

    /**
     * Extracts only digits from any raw phone string.
     */
    fun extractDigits(raw: String?): String {
        if (raw == null) return ""
        return raw.filter { it.isDigit() }
    }

    /**
     * Extracts the standard 10-digit national number (e.g. for Indian numbers).
     */
    fun extractNational10Digits(raw: String?): String {
        val digits = extractDigits(raw)
        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    /**
     * Formats phone into canonical E.164 (+<country_code><national_number>).
     */
    fun toE164(raw: String?, defaultCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        if (raw.isNullOrBlank()) return ""
        val trimmed = raw.trim()
        val digits = extractDigits(trimmed)
        if (digits.isEmpty()) return ""

        // If explicitly starts with '+', keep country code from digits
        if (trimmed.startsWith("+")) {
            return "+$digits"
        }

        // If starts with 00 (international access prefix)
        if (trimmed.startsWith("00")) {
            return "+" + digits.removePrefix("00")
        }

        // Standard national leading 0 (e.g. 09876543210 -> 9876543210)
        val withoutLeadingZero = if (digits.startsWith("0")) digits.removePrefix("0") else digits

        // If exactly 10 digits, prepend default country code
        return if (withoutLeadingZero.length == 10) {
            "+$defaultCountryCode$withoutLeadingZero"
        } else if (withoutLeadingZero.length > 10) {
            "+$withoutLeadingZero"
        } else {
            "+$withoutLeadingZero"
        }
    }

    /**
     * Checks if two phone number strings represent the same person.
     */
    fun areMatching(phone1: String?, phone2: String?): Boolean {
        if (phone1.isNullOrBlank() || phone2.isNullOrBlank()) return false
        val p1Clean = extractDigits(phone1)
        val p2Clean = extractDigits(phone2)

        if (p1Clean.isEmpty() || p2Clean.isEmpty()) return false
        if (p1Clean == p2Clean) return true

        val last10A = extractNational10Digits(phone1)
        val last10B = extractNational10Digits(phone2)

        return last10A.length >= 7 && last10A == last10B
    }

    /**
     * Returns a human-friendly formatted representation: +91 98765 43210
     */
    fun formatDisplay(raw: String?, defaultCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        val e164 = toE164(raw, defaultCountryCode)
        if (e164.length >= 12 && e164.startsWith("+")) {
            val ccLen = if (e164.startsWith("+$defaultCountryCode")) defaultCountryCode.length + 1 else 3
            val cc = e164.take(ccLen)
            val rest = e164.drop(ccLen)
            return if (rest.length == 10) {
                "$cc ${rest.take(5)} ${rest.drop(5)}"
            } else {
                "$cc $rest"
            }
        }
        return raw?.trim() ?: ""
    }
}
