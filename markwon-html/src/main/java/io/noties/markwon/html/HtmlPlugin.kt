package io.noties.markwon.html

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.tag.BlockquoteHandler
import io.noties.markwon.html.tag.EmphasisHandler
import io.noties.markwon.html.tag.HeadingHandler
import io.noties.markwon.html.tag.ImageHandler
import io.noties.markwon.html.tag.LinkHandler
import io.noties.markwon.html.tag.ListHandler
import io.noties.markwon.html.tag.StrikeHandler
import io.noties.markwon.html.tag.StrongEmphasisHandler
import io.noties.markwon.html.tag.SubScriptHandler
import io.noties.markwon.html.tag.SuperScriptHandler
import io.noties.markwon.html.tag.UnderlineHandler
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node

/**
 * @since 3.0.0
 */
class HtmlPlugin internal constructor() : AbstractMarkwonPlugin() {
    /**
     * @see .create
     * @since 4.0.0
     */
    interface HtmlConfigure {
        fun configureHtml(plugin: HtmlPlugin)
    }

    private val builder: MarkwonHtmlRendererImpl.Builder = MarkwonHtmlRendererImpl.Builder()

    private var htmlParser: MarkwonHtmlParser? = null
    private var htmlRenderer: MarkwonHtmlRenderer? = null

    // @since 4.4.0
    private var emptyTagReplacement = HtmlEmptyTagReplacement()

    /**
     * @param allowNonClosedTags whether or not non-closed tags should be closed
     * at the document end. By default `false`
     * @since 4.0.0
     */
    fun allowNonClosedTags(allowNonClosedTags: Boolean): HtmlPlugin {
        builder.allowNonClosedTags(allowNonClosedTags)
        return this
    }

    /**
     * @since 4.0.0
     */
    fun addHandler(tagHandler: TagHandler): HtmlPlugin {
        builder.addHandler(tagHandler)
        return this
    }

    /**
     * @since 4.0.0
     */
    fun getHandler(tagName: String): TagHandler? {
        return builder.getHandler(tagName)
    }

    /**
     * Indicate if HtmlPlugin should register default HTML tag handlers. Pass `true` to **not**
     * include default handlers. By default default handlers are included. You can use
     * [TagHandlerNoOp] to no-op certain default tags.
     *
     * @see TagHandlerNoOp
     *
     * @since 4.0.0
     */
    fun excludeDefaults(excludeDefaults: Boolean): HtmlPlugin {
        builder.excludeDefaults(excludeDefaults)
        return this
    }

    /**
     * @param emptyTagReplacement [HtmlEmptyTagReplacement]
     * @since 4.4.0
     */
    fun emptyTagReplacement(emptyTagReplacement: HtmlEmptyTagReplacement): HtmlPlugin {
        this.emptyTagReplacement = emptyTagReplacement
        return this
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        // @since 4.0.0 we init internal html-renderer here (marks the end of configuration)

        val builder = this.builder

        if (!builder.excludeDefaults()) {
            // please note that it's better to not checkState for
            // this method call (minor optimization), final `build` method call
            // will check for the state and throw an exception if applicable
            builder.addDefaultTagHandler(ImageHandler.create())
            builder.addDefaultTagHandler(LinkHandler())
            builder.addDefaultTagHandler(BlockquoteHandler())
            builder.addDefaultTagHandler(SubScriptHandler())
            builder.addDefaultTagHandler(SuperScriptHandler())
            builder.addDefaultTagHandler(StrongEmphasisHandler())
            builder.addDefaultTagHandler(StrikeHandler())
            builder.addDefaultTagHandler(UnderlineHandler())
            builder.addDefaultTagHandler(ListHandler())
            builder.addDefaultTagHandler(EmphasisHandler())
            builder.addDefaultTagHandler(HeadingHandler())
        }

        htmlParser = MarkwonHtmlParserImpl.create(emptyTagReplacement)
        htmlRenderer = builder.build()
    }

    override fun afterRender(node: Node, visitor: MarkwonVisitor) {
        val htmlRenderer = this.htmlRenderer
        if (htmlRenderer != null) {
            htmlRenderer.render(visitor, htmlParser!!)
        } else {
            throw IllegalStateException("Unexpected state, html-renderer is not defined")
        }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(
            HtmlBlock::class.java,
            MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, htmlBlock: HtmlBlock ->
                visitHtml(
                    visitor!!, htmlBlock.literal
                )
            }).on(
            HtmlInline::class.java,
            MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, htmlInline: HtmlInline ->
                visitHtml(
                    visitor!!, htmlInline.literal
                )
            })
    }

    private fun visitHtml(visitor: MarkwonVisitor, html: String?) {
        if (html != null) {
            htmlParser!!.processFragment(visitor.builder(), html)
        }
    }

    companion object {
        fun create(): HtmlPlugin {
            return HtmlPlugin()
        }

        /**
         * @since 4.0.0
         */
        fun create(configure: HtmlConfigure): HtmlPlugin {
            val plugin: HtmlPlugin = create()
            configure.configureHtml(plugin)
            return plugin
        }

        const val SCRIPT_DEF_TEXT_SIZE_RATIO: Float = .75f
    }
}
