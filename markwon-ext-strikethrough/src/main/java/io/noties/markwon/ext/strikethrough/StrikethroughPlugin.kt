package io.noties.markwon.ext.strikethrough

import android.text.style.StrikethroughSpan
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser

/**
 * Plugin to add strikethrough markdown feature. This plugin will extend commonmark-java.Parser
 * with strikethrough extension, add SpanFactory and register commonmark-java.Strikethrough node
 * visitor
 *
 * @see .create
 * @since 3.0.0
 */
class StrikethroughPlugin : AbstractMarkwonPlugin() {
    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(mutableSetOf<Extension>(StrikethroughExtension.create()))
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(
            Strikethrough::class.java
        ) { configuration, props -> StrikethroughSpan() }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(
            Strikethrough::class.java,
            MarkwonVisitor.NodeVisitor { visitor, strikethrough ->
                val length = visitor.length()
                visitor.visitChildren(strikethrough)
                visitor.setSpansForNodeOptional(strikethrough, length)
            })
    }

    companion object {
        fun create(): StrikethroughPlugin {
            return StrikethroughPlugin()
        }
    }
}
