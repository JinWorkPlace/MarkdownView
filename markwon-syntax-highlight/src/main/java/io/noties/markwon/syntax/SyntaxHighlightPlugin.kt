package io.noties.markwon.syntax

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.prism4j.Prism4j

class SyntaxHighlightPlugin(
    private val prism4j: Prism4j,
    private val theme: Prism4jTheme,
    private val fallbackLanguage: String?
) : AbstractMarkwonPlugin() {
    override fun configureTheme(builder: MarkwonTheme.Builder) {
        builder.codeTextColor(theme.textColor()).codeBackgroundColor(theme.background())
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.syntaxHighlight(Prism4jSyntaxHighlight.create(prism4j, theme, fallbackLanguage))
    }

    companion object {
        @JvmOverloads
        fun create(
            prism4j: Prism4j, theme: Prism4jTheme, fallbackLanguage: String? = null
        ): SyntaxHighlightPlugin {
            return SyntaxHighlightPlugin(prism4j, theme, fallbackLanguage)
        }
    }
}
