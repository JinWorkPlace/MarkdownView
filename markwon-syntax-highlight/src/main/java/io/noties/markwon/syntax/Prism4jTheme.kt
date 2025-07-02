package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import androidx.annotation.ColorInt
import io.noties.prism4j.Prism4j

interface Prism4jTheme {
    @ColorInt
    fun background(): Int

    @ColorInt
    fun textColor(): Int

    fun apply(
        language: String,
        syntax: Prism4j.Syntax,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    )
}
