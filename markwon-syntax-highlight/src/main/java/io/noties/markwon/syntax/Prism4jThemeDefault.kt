package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import androidx.annotation.ColorInt
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan

class Prism4jThemeDefault(@param:ColorInt private val background: Int) : Prism4jThemeBase() {
    override fun background(): Int {
        return background
    }

    override fun textColor(): Int {
        return -0x23000000
    }

    override fun init(): ColorHashMap {
        return ColorHashMap().add(-0x8f7f70, "comment", "prolog", "doctype", "cdata")
            .add(-0x666667, "punctuation")
            .add(-0x66ffab, "property", "tag", "boolean", "number", "constant", "symbol", "deleted")
            .add(-0x996700, "selector", "attr-name", "string", "char", "builtin", "inserted")
            .add(-0x6591c6, "operator", "entity", "url")
            .add(-0xff8856, "atrule", "attr-value", "keyword")
            .add(-0x22b598, "function", "class-name")
            .add(-0x116700, "regex", "important", "variable")
    }

    override fun applyColor(
        language: String,
        type: String,
        alias: String?,
        @ColorInt color: Int,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    ) {
        var color = color
        if ("css" == language && isOfType("string", type, alias)) {
            super.applyColor(language, type, alias, -0x6591c6, builder, start, end)
            builder.setSpan(
                BackgroundColorSpan(-0x7f000001), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return
        }

        if (isOfType("namespace", type, alias)) {
            color = applyAlpha(.7f, color)
        }

        super.applyColor(language, type, alias, color, builder, start, end)

        if (isOfType("important", type, alias) || isOfType("bold", type, alias)) {
            builder.setSpan(StrongEmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (isOfType("italic", type, alias)) {
            builder.setSpan(EmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        fun create(): Prism4jThemeDefault {
            return Prism4jThemeDefault(-0xa0d10)
        }

        /**
         * @since 3.0.0
         */
        fun create(@ColorInt background: Int): Prism4jThemeDefault {
            return Prism4jThemeDefault(background)
        }
    }
}
