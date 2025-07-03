package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import android.text.TextUtils
import io.noties.prism4j.Prism4j

open class Prism4jSyntaxHighlight protected constructor(
    private val prism4j: Prism4j, private val theme: Prism4jTheme, private val fallback: String?
) : SyntaxHighlight {
    override fun highlight(info: String?, code: String): CharSequence {
        // @since 4.2.2
        // although not null, but still is empty

        if (code.isEmpty()) {
            return code
        }

        // if info is null, do not highlight -> LICENCE footer very commonly wrapped inside code
        // block without syntax name specified (so, do not highlight)
        return if (info == null) highlightNoLanguageInfo(code)
        else highlightWithLanguageInfo(info, code)
    }

    protected fun highlightNoLanguageInfo(code: String): CharSequence {
        return code
    }

    protected fun highlightWithLanguageInfo(info: String, code: String): CharSequence {
        val out: CharSequence

        val language: String?
        val grammar: Prism4j.Grammar?
        run {
            var _language: String? = info
            var _grammar = prism4j.grammar(info)
            if (_grammar == null && !TextUtils.isEmpty(fallback)) {
                _language = fallback
                _grammar = prism4j.grammar(fallback!!)
            }
            language = _language
            grammar = _grammar
        }

        out = if (grammar != null) {
            highlight(language!!, grammar, code)
        } else {
            code
        }

        return out
    }

    protected fun highlight(
        language: String, grammar: Prism4j.Grammar, code: String
    ): CharSequence {
        val builder = SpannableStringBuilder()
        val visitor = Prism4jSyntaxVisitor(language, theme, builder)
        visitor.visit(prism4j.tokenize(code, grammar))
        return builder
    }

    protected fun prism4j(): Prism4j {
        return prism4j
    }

    protected fun theme(): Prism4jTheme {
        return theme
    }

    protected fun fallback(): String? {
        return fallback
    }

    companion object {
        fun create(
            prism4j: Prism4j, theme: Prism4jTheme
        ): Prism4jSyntaxHighlight {
            return Prism4jSyntaxHighlight(prism4j, theme, null)
        }

        @JvmStatic
        fun create(
            prism4j: Prism4j, theme: Prism4jTheme, fallback: String?
        ): Prism4jSyntaxHighlight {
            return Prism4jSyntaxHighlight(prism4j, theme, fallback)
        }
    }
}
