package com.livewire.app.authentication

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.livewire.app.R

class CheckedPasswordEditText : AppCompatEditText {
    private val checkDrawable = ContextCompat.getDrawable(context, R.drawable.ic_green_checkmark)!!
    private var checked = false

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup()
    }

    private fun setup() {
        typeface = ResourcesCompat.getFont(context, R.font.roboto_mono_regular)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (text?.isEmpty() != false) return

        val passwordViewToggle = compoundDrawables[2]
        var passwordToggleWidth = 0
        if (passwordViewToggle != null) {
            passwordToggleWidth = passwordViewToggle.bounds.right - passwordViewToggle.bounds.left
        }
        if (checked) {
            val r = measuredWidth - paddingRight - passwordToggleWidth
            val l = r - checkDrawable.intrinsicWidth
            val t = top + (height - checkDrawable.intrinsicHeight) / 2
            val b = t + checkDrawable.intrinsicHeight

            checkDrawable.setBounds(l, t, r, b)
            checkDrawable.draw(canvas)
        }
    }

    fun setChecked(checked: Boolean) {
        if (this.checked != checked) {
            this.checked = checked
            refreshDrawableState()
            invalidate()
        }
    }
}
