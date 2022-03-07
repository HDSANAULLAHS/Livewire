package com.livewire.app.utils

import android.animation.ObjectAnimator
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import androidx.core.text.toSpannable
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout
import com.livewire.app.R
// View
inline var View?.visible
    get() = (this?.visibility ?: View.GONE) == View.VISIBLE
    set(value) {
        this?.visibility = if (value) View.VISIBLE else View.GONE
    }

fun View?.hideKeyboard() {
    if (this != null) {
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun View?.showKeyboard() {
    if (this != null) {
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, 0)
    }
}

fun View?.absoluteX(): Int {
    val location = IntArray(2)
    this?.getLocationOnScreen(location)
    return location[0]
}

fun View?.absoluteY(): Int {
    val location = IntArray(2)
    this?.getLocationOnScreen(location)
    return location[1]
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}

// Provides concise syntax for just attaching onTextChanged to TextView
// view.watchText { text -> ... }
fun TextView.watchText(callback: (TextView) -> Unit): TextWatcher {
    var tv = this
    var lastText: String? = null

    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Textview sometimes fires text change when it didnt actually change (i.e., dropdown shown)
            if (lastText?.equals(tv.text.toString()) == true) {
                return
            }

            lastText = tv.text.toString()
            callback(tv)
        }
    }

    addTextChangedListener(textWatcher)

    return textWatcher
}

fun TextView.watchText(callback: (String) -> Unit, delayed: (String) -> Unit, delayTime: Long = 250): TextWatcher {
    val handler = Handler()

    val runnable = Runnable {
        delayed(this.text.toString())
    }

    return this.watchText {
        callback(this.text.toString())
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, delayTime)
    }
}

fun TextView.setUnderlineText(text: String?) {
    if (text.isNullOrBlank()) {
        this.visible = false
        return
    }

    val content = text.toSpannable()
    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    this.text = content
    this.visible = true
}

fun TextView.linkStarredText(links: Map<String, String>, clicked: (String) -> Unit) {
    if (text.isNullOrBlank())
        return

    val spannable = SpannableStringBuilder(text)

    links.forEach {
        val beginIndex = text.indexOf(it.key, 0)
        if (beginIndex != -1) {
            val endIndex = beginIndex + it.key.length
            spannable.setSpan(
                UnderlineSpan(),
                beginIndex,
                endIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        widget.cancelPendingInputEvents()
                        clicked(it.value)
                    }

                    override fun updateDrawState(ds: TextPaint) {}
                }, beginIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }

    this.text = spannable
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.linkStarredText( link: String,makeBold: Boolean = true, clicked: (String) -> Unit) {
    val original = text ?: return
    val key = "**"

    val start = original.indexOf(key)
    val end = original.indexOf(key, start + 1)
    if (start < 0 || end < 0) {
        return
    }


    val spans = if (makeBold) {
        listOf(
                UnderlineSpan(),
                StyleSpan(Typeface.BOLD),
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        widget.cancelPendingInputEvents()

                        clicked(link)
                    }

                    override fun updateDrawState(ds: TextPaint) {}
                })
    } else {
        listOf(
                UnderlineSpan(),
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        widget.cancelPendingInputEvents()

                        clicked(link)
                    }

                    override fun updateDrawState(ds: TextPaint) {}
                })
    }

    val spannable = SpannableStringBuilder(original)
            .replace(start, start + key.length, "")
            .replace(end - key.length, end, "")
    spans.forEach { spannable.setSpan(it, start, end - key.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE) }
    this.text = spannable

    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.boldPartOfText(context: Context, @StringRes wholeTextResId: Int, @StringRes vararg boldPartResIds: Int) {
    val boldParts = boldPartResIds.map { context.getString(it) }
    val wholeText = context.getString(wholeTextResId, *boldParts.toTypedArray())
    val spannable = wholeText.toSpannable()
    boldParts.forEach {
        val indexOfBoldPart = spannable.indexOf(it)
        spannable.setSpan(StyleSpan(Typeface.BOLD), indexOfBoldPart, indexOfBoldPart + it.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    text = spannable
}


fun TextView.setTextAndVisibility(text: String?, hiddenVisibility: Int = View.GONE) {
    if (text.isNullOrEmpty()) {
        this.visibility = hiddenVisibility
    } else {
        this.text = text
        this.visibility = View.VISIBLE
    }
}

fun TextView.setWebsiteText(websiteUrl: String?) {
    this.setUnderlineText(websiteUrl?.removePrefix("http://")?.removePrefix("https://")?.removePrefix("www."))
    this.setOnClickListener { context.openExternalBrowser(websiteUrl) }
}

fun TextView.setPhoneNumberText(phoneNumber: String?) {
    this.setUnderlineText(phoneNumber)
    this.setOnClickListener { context.startDialer(phoneNumber) }
}

fun TextView.shrinkToFit(minSizeSp: Float = 10f, stepSize: Float = 2f) {
    val minSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minSizeSp, context.resources.displayMetrics)
    val startSizePx = this.textSize

    // Only use this on single line textviews. Otherwise use the
    // autoSizeTextView appcompat solution that uses height as well
    if (maxLines != 1) {
        throw RuntimeException("Only use shrinkToFit on single line textviews")
    }

    fun shrink() {
        // Not measured yet
        if (width == 0 || text.isNullOrBlank() || visibility == View.GONE) {
            return
        }

        // Get text including transformations (i.e., all caps)
        val drawText = transformationMethod?.getTransformation(text, this) ?: text

        var size = startSizePx
        while (size >= minSizePx) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)

            val textWidth = paint.measureText(drawText, 0, drawText.length)
            if (textWidth < (width - paddingStart - paddingEnd - 1)) {
                break
            }

            size -= stepSize
        }
    }

    onLayout { shrink() }
    watchText { shrink() }
}

private class TopAlignSuperscriptSpan(private val textSizeScalePercentage: Float = 0.5f) : MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint) {
        updateAnyState(tp)
    }

    override fun updateMeasureState(tp: TextPaint) {
        updateAnyState(tp)
    }

    private fun updateAnyState(tp: TextPaint) {
        val bounds = Rect()
        tp.getTextBounds("1A", 0, 2, bounds)
        val originalHeight = bounds.height()
        tp.textSize = tp.textSize * textSizeScalePercentage
        tp.getTextBounds("1A", 0, 2, bounds)
        val newHeight = bounds.height()
        tp.baselineShift += (newHeight - originalHeight) + 1
    }
}

fun View.setHeight(height: Int) {
    this.updateLayoutParams {
        this.height = height
    }
}

fun View.setWidth(width: Int) {
    this.updateLayoutParams {
        this.width = width
    }
}

fun View.setSize(width: Int, height: Int) {
    this.updateLayoutParams {
        this.width = width
        this.height = height
    }
}

fun View.setMarginTop(margin: Int) {
    this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        topMargin = margin
    }
}

fun View.setMarginBottom(margin: Int) {
    this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        bottomMargin = margin
    }
}

fun View.setMargins(top: Int, bottom: Int) {
    this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        topMargin = top
        bottomMargin = bottom
    }
}

fun ViewGroup.toggleBottomSheetExpanded() {
    val behavior = BottomSheetBehavior.from(this)
    val collapsed = behavior.state == BottomSheetBehavior.STATE_COLLAPSED
    behavior.state = if (collapsed) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
}

fun ViewGroup.expandBottomSheet() {
    val behavior = BottomSheetBehavior.from(this)
    if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

fun ViewGroup.collapseBottomSheet() {
    val behavior = BottomSheetBehavior.from(this)
    if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}

fun ViewGroup.onBottomSheetHidden(callback: () -> Unit) {
    val bottomSheetBehavior = BottomSheetBehavior.from(this)

    bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                callback()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    })
}

fun Toolbar.setTransparencyOnSheetSlide(slidePercent: Float?) {
    val percent = Math.max(0f, Math.min(1f, slidePercent ?: 1f))

    // Set toolbar to gone so it wont take touch events (it is over the card)
    this.visible = percent > 0.1f

    if (percent > 0.2f) {
        this.alpha = 1f
        return
    }

    this.alpha = (percent - 0.1f) * 10f
}

fun Context.startDialer(number: String?) {
    number ?: return

    val call = Uri.parse("tel:" + number)
    startActivity(Intent(Intent.ACTION_DIAL, call))
}

fun Context.openExternalBrowser(websiteUrl: String?) {
    websiteUrl ?: return
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
}

fun String?.nullIfEmpty(): String? {
    return if (this.isNullOrEmpty()) null else this
}

private val digitsRegex = "\\d+".toRegex()
fun String.isAllDigits(): Boolean {
    return digitsRegex.matches(this)
}

private val alphaRegex = "[0-9A-Z]+".toRegex()
fun String.isAllAlphanumeric(): Boolean {
    return alphaRegex.matches(this)
}

fun String?.extractDigits(): String {
    this ?: return ""

    val sb = StringBuilder()

    for (ch in this) {
        if (ch.isDigit()) {
            sb.append(ch)
        }
    }

    return sb.toString()
}

fun String?.extractNumberOrRange(): Int {
    return this?.split("-")?.lastOrNull()?.extractDigits()?.toIntOrNull() ?: 0
}

fun String.extractAlphaNumeric(): String {
    val sb = StringBuilder()

    for (ch in this) {
        if (ch.isLetterOrDigit()) {
            sb.append(ch)
        }
    }

    return sb.toString()
}

fun StringBuilder.appendSpaced(value: String?) {
    value.nullIfEmpty()?.let {
        if (this.isNotEmpty()) {
            this.append(" ")
        }

        this.append(it)
    }
}

fun View.onLayout(callback: () -> Unit) {
    this.doOnLayout { callback() }
}

fun RecyclerView.ensureVisible(position: Int) {
    val lm = this.layoutManager as LinearLayoutManager
    val first = lm.findFirstCompletelyVisibleItemPosition()
    val last = lm.findLastCompletelyVisibleItemPosition()

    if (position < first) {
        this.scrollToPosition(position)
    } else if (position > last) {
        this.scrollToPosition(position)
    }
}

fun ScrollView.smoothScrollAnimate(y: Int) {
    ObjectAnimator.ofInt(this, "scrollY", y).apply {
        interpolator = DecelerateInterpolator()
        duration = 800
        start()
    }
}

fun NestedScrollView.smoothScrollAnimate(y: Int) {
    ObjectAnimator.ofInt(this, "scrollY", y).apply {
        interpolator = DecelerateInterpolator()
        duration = 800
        start()
    }
}

/**
 * View Pager
 */

fun ViewPager.pageChanged(changed: (Int) -> Unit) {
    this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            changed(position)
        }
    })
}

fun ViewPager2.pageChanged(changed: (Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            changed(position)
        }
    })
}

/**
 * Validation
 */

class ClearErrorTextWatcher(val et: EditText) : TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        et.setCompoundDrawables(null, null, null, null)

        // Do not immediately remove the text change listener. The list of listeners is being looped
        // over in the text view and causes IndexOutOfBoundsException. Remove it after the loop completes.
        et.post {
            et.removeTextChangedListener(this)
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
}


@Suppress("DEPRECATION")
fun TextView.setHtmlText(htmlText: String, imageGetter: Html.ImageGetter? = null, tagHandler: Html.TagHandler? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT, imageGetter, tagHandler)
    } else {
        this.text = Html.fromHtml(htmlText, imageGetter, tagHandler)
    }
}

fun Double.toStringWithSign(): String {
    val plusSign = if (this > 0) "+" else ""
    return "$plusSign$this"
}

fun String?.stripHtml(singleLine: Boolean = true): String {
    this ?: return ""

    // Text (i.e., from Here) comes back with a mix of HTML and plain text (i.e., \n)
    // but Html.fromHtml will drop those newlines needed to preserve
    val input = if (singleLine) {
        this.replace("\n", ", ")
    } else {
        this
    }

    // Convert to spanned
    val html = input.toHtml()

    // Drop tags with toString() and return
    return html.toString().trim()
}

@Suppress("DEPRECATION")
fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
    canvas.withTranslation(x, y) {
        draw(this)
    }
}

fun EditText.preventFocus() {
    // Use when clicking on an edit text shows something else (i.e. date picker) instead of typing text
    this.inputType = InputType.TYPE_NULL
}

fun TextInputLayout.setPasswordFont() {
    // Android ignores font set in XML, but it works programmatically
    this.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_mono_regular)
}

inline fun Int.ifNonZero(block: (Int) -> Unit) {
    if (this != 0) {
        block(this)
    }
}

fun <T> MutableList<T>.moveItem(oldIndex: Int, newIndex: Int) {
    val item = this[oldIndex]
    removeAt(oldIndex)
    if (oldIndex > newIndex)
        add(newIndex, item)
    else
        add(newIndex - 1, item)
}

inline fun <reified T : Enum<T>> SharedPreferences.getEnumSet(key: String, defaultValues: List<T> = listOf()): Set<T> {
    val stringValues = this.getStringSet(key, null)

    return if (stringValues != null) {
        stringValues.map { it.replace(' ', '_').toUpperCase() }
                .mapNotNull { name -> enumValues<T>().firstOrNull { it.name == name } }
                .toSet()
    } else {
        defaultValues.toSet()
    }
}

inline fun <reified T : Enum<T>> SharedPreferences.putEnumSet(key: String, values: Set<T>) {
    this.edit { putStringSet(key, values.map { it.name }.toSet()) }
}

fun TypedArray.getCharSequence(index: Int): CharSequence {
    val tv = TypedValue()

    if (!getValue(index, tv)) {
        return ""
    }

    return tv.string
}


fun Float.pxToDp(): Float {
    return (this / Resources.getSystem().displayMetrics.density)
}

fun Float.dpToPx(): Float {
    return (this * Resources.getSystem().displayMetrics.density)
}



fun NestedScrollView.releasePinnedHeaderView() {
    this.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
}

fun Spinner.itemSelected(handler: () -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            handler()
        }

    }

}