package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import io.noties.prism4j.Prism4j

abstract class Prism4jThemeBase protected constructor() : Prism4jTheme {
    private val colorHashMap: ColorHashMap

    init {
        this.colorHashMap = init()
    }

    protected abstract fun init(): ColorHashMap

    @ColorInt
    protected fun color(type: String, alias: String?): Int {
        var color = colorHashMap[type]
        if (color == null && alias != null) {
            color = colorHashMap.get(alias)
        }

        return color?.color ?: 0
    }

    override fun apply(
        language: String,
        syntax: Prism4j.Syntax,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    ) {
        val type = syntax.type()
        val alias = syntax.alias()

        val color = color(type, alias)
        if (color != 0) {
            applyColor(language, type, alias, color, builder, start, end)
        }
    }

    @Suppress("unused")
    protected open fun applyColor(
        language: String,
        type: String,
        alias: String?,
        @ColorInt color: Int,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    ) {
        builder.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    protected open class Color protected constructor(@field:ColorInt @param:ColorInt val color: Int) {
        companion object {
            fun of(@ColorInt color: Int): Color {
                return Color(color)
            }
        }
    }

    protected class ColorHashMap : HashMap<String?, Color?>() {
        fun add(@ColorInt color: Int, name: String?): ColorHashMap {
            put(name, Color.Companion.of(color))
            return this
        }

        fun add(
            @ColorInt color: Int, name1: String, name2: String
        ): ColorHashMap {
            val c: Color = Color.Companion.of(color)
            put(name1, c)
            put(name2, c)
            return this
        }

        fun add(
            @ColorInt color: Int, name1: String, name2: String, name3: String
        ): ColorHashMap {
            val c: Color = Color.Companion.of(color)
            put(name1, c)
            put(name2, c)
            put(name3, c)
            return this
        }

        fun add(@ColorInt color: Int, vararg names: String?): ColorHashMap {
            val c: Color = Color.Companion.of(color)
            for (name in names) {
                put(name, c)
            }
            return this
        }
    }

    companion object {
        @ColorInt
        protected fun applyAlpha(
            @IntRange(from = 0, to = 255) alpha: Int, @ColorInt color: Int
        ): Int {
            return (color and 0x00FFFFFF) or (alpha shl 24)
        }

        @JvmStatic
        @ColorInt
        protected fun applyAlpha(
            @FloatRange(from = 0.0, to = 1.0) alpha: Float, @ColorInt color: Int
        ): Int {
            return applyAlpha((255 * alpha + .5f).toInt(), color)
        }

        @JvmStatic
        protected fun isOfType(expected: String, type: String, alias: String?): Boolean {
            return expected == type || expected == alias
        }
    }
}
