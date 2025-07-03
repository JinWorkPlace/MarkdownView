package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.ColorInt
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan

class Prism4jThemeDarkula(
    @param:ColorInt private val background: Int
) : Prism4jThemeBase() {
    override fun background(): Int {
        return background
    }

    override fun textColor(): Int {
        return -0x56483a
    }

    override fun init(): ColorHashMap {
        return ColorHashMap().add(-0x7f7f80, "comment", "prolog", "cdata")
            .add(-0x3387ce, "delimiter", "boolean", "keyword", "selector", "important", "atrule")
            .add(-0x56483a, "operator", "punctuation", "attr-name")
            .add(-0x174096, "tag", "doctype", "builtin")
            .add(-0x976845, "entity", "number", "symbol")
            .add(-0x678956, "property", "constant", "variable").add(-0x9578a7, "string", "char")
            .add(-0x444bc8, "annotation").add(-0x5a3d9f, "attr-value").add(-0xd78422, "url")
            .add(-0x3993, "function").add(-0xc9becb, "regex").add(-0xd6bbca, "inserted")
            .add(-0xb7b5b6, "deleted")
    }

    override fun applyColor(
        language: String,
        type: String,
        alias: String?,
        color: Int,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    ) {
        super.applyColor(language, type, alias, color, builder, start, end)

        if (isOfType("important", type, alias) || isOfType("bold", type, alias)) {
            builder.setSpan(StrongEmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (isOfType("italic", type, alias)) {
            builder.setSpan(EmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        fun create(): Prism4jThemeDarkula {
            return Prism4jThemeDarkula(-0xd2d2d3)
        }

        /**
         * @param background color
         * @since 3.0.0
         */
        fun create(@ColorInt background: Int): Prism4jThemeDarkula {
            return Prism4jThemeDarkula(background)
        }
    }
}
