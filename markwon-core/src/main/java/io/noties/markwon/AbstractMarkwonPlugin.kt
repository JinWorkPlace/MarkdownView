package io.noties.markwon

import android.text.Spanned
import android.widget.TextView
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.node.Node
import org.commonmark.parser.Parser

/**
 * Class that extends [MarkwonPlugin] with all methods implemented (empty body)
 * for easier plugin implementation. Only required methods can be overriden
 *
 * @see MarkwonPlugin
 *
 * @since 3.0.0
 */
abstract class AbstractMarkwonPlugin : MarkwonPlugin {
    override fun configure(registry: MarkwonPlugin.Registry) {
    }

    override fun configureParser(builder: Parser.Builder) {
    }

    override fun configureTheme(builder: MarkwonTheme.Builder) {
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
    }

    override fun processMarkdown(markdown: String): String {
        return markdown
    }

    override fun beforeRender(node: Node) {
    }

    override fun afterRender(node: Node, visitor: MarkwonVisitor) {
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
    }

    override fun afterSetText(textView: TextView) {
    }
}
