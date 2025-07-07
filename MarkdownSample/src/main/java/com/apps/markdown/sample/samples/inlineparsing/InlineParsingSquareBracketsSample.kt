package com.apps.markdown.sample.samples.inlineparsing

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.inlineparser.InlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser.FactoryBuilder
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin.BuilderConfigure
import io.noties.markwon.inlineparser.OpenBracketInlineProcessor
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

@MarkwonSampleInfo(
    id = "20200819071751",
    title = "Inline Parsing of square brackets",
    description = ("Disable OpenBracketInlineParser in order " + "to parse own markdown syntax based on `[` character(s). This would disable native " + "markdown [links](#) but not images ![image-alt](#)"),
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING]
)
class InlineParsingSquareBracketsSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Hello\n" + "Hey! [[my text]] here and what to do with it?\n\n" + "[[at the beginning]] of a line with [links](#) disabled"

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(MarkwonInlineParserPlugin.create(object : BuilderConfigure<FactoryBuilder> {
                override fun configureBuilder(factoryBuilder: FactoryBuilder) {
                    factoryBuilder.addInlineProcessor(MyTextInlineProcessor())
                        .excludeInlineProcessor(OpenBracketInlineProcessor::class.java)
                }
            })).usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(MyTextNode::class.java, GenericInlineNodeVisitor()).on(
                        NotMyTextNode::class.java, GenericInlineNodeVisitor()
                    )
                }

                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.setFactory(
                        MyTextNode::class.java, object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                return ForegroundColorSpan(Color.RED)
                            }
                        }).setFactory(
                        NotMyTextNode::class.java, object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                return ForegroundColorSpan(Color.GREEN)
                            }
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }

    private class GenericInlineNodeVisitor : MarkwonVisitor.NodeVisitor<Node> {
        override fun visit(visitor: MarkwonVisitor, node: Node) {
            val length: Int = visitor.length()
            visitor.visitChildren(node)
            visitor.setSpansForNodeOptional(node, length)
        }
    }

    private class MyTextInlineProcessor : InlineProcessor() {
        override fun specialCharacter(): Char {
            return '['
        }

        override fun parse(): Node? {
            val match = match(RE)
            if (match != null) {
                // consume syntax
                val text = match.substring(2, match.length - 2)
                // for example some condition checking
                val node = if (text == "my text") {
                    MyTextNode()
                } else {
                    NotMyTextNode()
                }
                node.appendChild(text(text))
                return node
            }
            return null
        }

        companion object {
            private val RE: java.util.regex.Pattern =
                java.util.regex.Pattern.compile("\\[\\[(.+?)]]")
        }
    }

    private class MyTextNode : CustomNode()

    private class NotMyTextNode : CustomNode()
}
