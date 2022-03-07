package com.livewire.app.authentication

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.livewire.app.R
import com.livewire.app.utils.setPasswordFont
import kotlinx.android.synthetic.main.view_set_password.view.*


class SetPasswordView : FrameLayout {
    private val validator = AccountFieldValidator()

    constructor(context: Context) : super(context) {
        setup(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup(attrs)
    }

    val passwordText: Editable?
        get() = this.password.text


    var isEditPassword: Boolean = false

    private fun setup(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_set_password, this, true)

        attrs?.let { att ->
            context.obtainStyledAttributes(att, R.styleable.SetPasswordView).use {
                it.getString(R.styleable.SetPasswordView_passwordHint)?.let { passwordTextInputLayout.hint = it }
                isEditPassword = it.getBoolean(R.styleable.SetPasswordView_isEditPassword, false)
            }
        }
        if (isEditPassword) {
            passwordTitle.text = context.getString(R.string.new_password)
        }

        if (!isInEditMode) {
            setValidIndicator(eightPlusCharactersInfo, false)
            setValidIndicator(uppercaseCharInfo, false)
            setValidIndicator(lowercaseCharInfo, false)
            setValidIndicator(oneNumberInfo, false)
            setValidIndicator(specialCharInfo, false)

            password.addTextChangedListener(textWatcher)

            passwordTextInputLayout.setPasswordFont()
        }
        password.transformationMethod = AsteriskPasswordTransformation()

        password.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER) {

                if (event == null || !event.isShiftPressed) {
                    // the user is done typing
                    val userPassword = password.text.toString().trim()

                    requirement_not_met_user_info.isVisible = isPasswordValid(userPassword).not()

                    if (validator.validatePasswordLength(userPassword).not()) {
                        eightPlusCharactersInfo.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_alert_error, 0, 0, 0)
                        }
                    }
                    if (validator.validatePasswordUppercase(userPassword).not()) {
                        uppercaseCharInfo.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_alert_error, 0, 0, 0)
                        }
                    }
                    if (validator.validatePasswordLowercase(userPassword).not()) {
                        lowercaseCharInfo.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_alert_error, 0, 0, 0)
                        }
                    }
                    if (validator.validatePasswordNumber(userPassword).not()) {
                        oneNumberInfo.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_alert_error, 0, 0, 0)
                        }
                    }
                    if (validator.validatePasswordSpecialCharacter(userPassword).not()) {
                        specialCharInfo.apply {
                            setTextColor(ContextCompat.getColor(context, R.color.red))
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_alert_error, 0, 0, 0)
                        }
                    }
                }
            }
            false
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return validator.validatePassword(password, password) == 0
    }

    private fun isConfirmPasswordValid(password: String, confirmPassword: String): Boolean {
        return validator.validatePassword(password, confirmPassword) == 0
    }

    private fun validatePassword() {
        val password = password.text.toString().trim()
        //val confirmPassword = confirmPassword.text.toString().trim()

        this.password.setChecked(isPasswordValid(password))
        //this.confirmPassword.setChecked(isConfirmPasswordValid(password, confirmPassword))

        setValidIndicator(eightPlusCharactersInfo, validator.validatePasswordLength(password))
        setValidIndicator(uppercaseCharInfo, validator.validatePasswordUppercase(password))
        setValidIndicator(lowercaseCharInfo, validator.validatePasswordLowercase(password))
        setValidIndicator(specialCharInfo, validator.validatePasswordSpecialCharacter(password))
        setValidIndicator(oneNumberInfo, validator.validatePasswordNumber(password))
    }

    private fun setValidIndicator(view: TextView, valid: Boolean) {
        if (valid) {
            view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_green_checkmark, 0, 0, 0)
            view.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shape_black_indicator, 0, 0, 0)
            view.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    private val textWatcher = object : android.text.TextWatcher {
        override fun afterTextChanged(s: Editable) {
            validatePassword()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }
    private val confirmPasswordTextWatcher = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val password = password.text.toString().trim()
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}
