package com.livewire.audax.authentication

import android.util.Patterns
import com.livewire.audax.R

class AccountFieldValidator {
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

    }

    // These are split into different checks because the UI shows
    // an indicator for each individual one
    private val specialCharacterRegex = Regex(".*[^A-Za-z0-9].*")
    private val numberRegex = Regex(".*\\d.*")
    private val uppercaseRegex = Regex(".*[A-Z]+.*")
    private val lowercaseRegex = Regex(".*[a-z]+.*")



    fun validateEmail(email: String): Int {
        if (email.isBlank()) {
            return R.string.missing_email
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return R.string.invalid_email
        }

        return 0
    }

    fun validatePasswordLength(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun validatePasswordSpecialCharacter(password: String): Boolean {
        return password.matches(specialCharacterRegex)
    }

    fun validatePasswordNumber(password: String): Boolean {
        return password.matches(numberRegex)
    }

    fun validatePasswordUppercase(password: String): Boolean {
        return password.matches(uppercaseRegex)
    }

    fun validatePasswordLowercase(password: String): Boolean {
        return password.matches(lowercaseRegex)
    }

    fun validatePassword(password: String, confirm: String): Int {
        if (password.isEmpty()) {
            return R.string.missing_password
        }

        if (!validatePasswordLength(password)) {
            return R.string.password_length
        }

        if (!validatePasswordSpecialCharacter(password)) {
            return R.string.password_special_char
        }

        if (!validatePasswordNumber(password)) {
            return R.string.password_digit
        }

        if (!validatePasswordUppercase(password)) {
            return R.string.password_upper_letter
        }

        if (!validatePasswordLowercase(password)) {
            return R.string.password_lower_letter
        }

        if (confirm.isEmpty()) {
            return R.string.confirm_password_missing
        }

        if (password != confirm) {
            return R.string.passwords_dont_match
        }

        return 0
    }

    fun validateName(firstName: String, lastName: String): Int {
        if (firstName.isBlank()) {
            return R.string.first_name_missing
        }

        if (lastName.isBlank()) {
            return R.string.last_name_missing
        }

        return 0
    }
}
