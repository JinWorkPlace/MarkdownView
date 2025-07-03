package io.noties.markwon.core.spans

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * A span implementation that allow applying custom Typeface. Although it is
 * not used directly by the library, it\'s helpful for customizations.
 *
 *
 * Please note that this implementation does not validate current paint state
 * and won\'t be updating/modifying supplied Typeface unless `mergeStyles` is specified
 *
 * @since 3.0.0
 */
class CustomTypefaceSpan internal constructor(
    private val typeface: Typeface, private val mergeStyles: Boolean
) : MetricAffectingSpan() {
    @Deprecated(
        """4.6.1 use {{@link #create(Typeface)}}
      or {@link #create(Typeface, boolean)} factory method"""
    )
    constructor(typeface: Typeface) : this(typeface, false)


    override fun updateMeasureState(paint: TextPaint) {
        updatePaint(paint)
    }

    override fun updateDrawState(paint: TextPaint) {
        updatePaint(paint)
    }

    private fun updatePaint(paint: TextPaint) {
        val oldTypeface = paint.typeface
        if (!mergeStyles || oldTypeface == null || oldTypeface.style == Typeface.NORMAL) {
            paint.typeface = typeface
        } else {
            val oldStyle = oldTypeface.style

            @SuppressLint("WrongConstant") val want = oldStyle or typeface.style
            val styledTypeface = Typeface.create(typeface, want)

            paint.typeface = styledTypeface
        }
    }

    companion object {
        /**
         * **NB!** in order to *merge* typeface styles, supplied typeface must be
         * able to be created via `Typeface.create(Typeface, int)` method. This would mean that bundled fonts
         * inside `assets` folder would be able to display styles properly.
         *
         * @param mergeStyles control if typeface styles must be merged, for example, if
         * this span (bold) is contained by other span (italic),
         * `mergeStyles=true` would result in bold-italic
         * @since 4.6.1
         */
        @JvmOverloads
        fun create(typeface: Typeface, mergeStyles: Boolean = false): CustomTypefaceSpan {
            return CustomTypefaceSpan(typeface, mergeStyles)
        }
    }
}
