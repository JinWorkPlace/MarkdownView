package io.noties.markwon.core

import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.factory.BlockQuoteSpanFactory
import io.noties.markwon.core.factory.CodeBlockSpanFactory
import io.noties.markwon.core.factory.CodeSpanFactory
import io.noties.markwon.core.factory.EmphasisSpanFactory
import io.noties.markwon.core.factory.HeadingSpanFactory
import io.noties.markwon.core.factory.LinkSpanFactory
import io.noties.markwon.core.factory.ListItemSpanFactory
import io.noties.markwon.core.factory.StrongEmphasisSpanFactory
import io.noties.markwon.core.factory.ThematicBreakSpanFactory
import io.noties.markwon.core.spans.OrderedListItemSpan.Companion.measure
import io.noties.markwon.core.spans.TextViewSpan.Companion.applyTo
import io.noties.markwon.image.ImageProps
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

/**
 * @see CoreProps
 *
 * @since 3.0.0
 */
open class CorePlugin protected constructor() : AbstractMarkwonPlugin() {
    /**
     * @see .addOnTextAddedListener
     * @since 4.0.0
     */
    interface OnTextAddedListener {
        /**
         * Will be called when new text is added to resulting [io.noties.markwon.SpannableBuilder].
         * Please note that only text represented by [Text] node will trigger this callback
         * (text inside code and code-blocks won\'t trigger it).
         *
         *
         * Please note that if you wish to add spans you must use `start` parameter
         * in order to place spans correctly (`start` represents the index at which `text`
         * was added). So, to set a span for the whole length of the text added one should use:
         *
         *
         * `visitor.builder().setSpan(new MySpan(), start, start + text.length(), 0);
        ` *
         *
         * @param visitor [MarkwonVisitor]
         * @param text    literal that had been added
         * @param start   index in `visitor` as which text had been added
         * @see .addOnTextAddedListener
         */
        fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int)
    }

    // @since 4.0.0
    private val onTextAddedListeners: MutableList<OnTextAddedListener> =
        ArrayList<OnTextAddedListener>(0)

    // @since 4.5.0
    private var hasExplicitMovementMethod = false

    /**
     * @since 4.5.0
     */
    fun hasExplicitMovementMethod(hasExplicitMovementMethod: Boolean): CorePlugin {
        this.hasExplicitMovementMethod = hasExplicitMovementMethod
        return this
    }

    /**
     * Can be useful to post-process text added. For example for auto-linking capabilities.
     *
     * @see OnTextAddedListener
     *
     * @since 4.0.0
     */
    fun addOnTextAddedListener(onTextAddedListener: OnTextAddedListener): CorePlugin {
        onTextAddedListeners.add(onTextAddedListener)
        return this
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        text(builder)
        strongEmphasis(builder)
        emphasis(builder)
        blockQuote(builder)
        code(builder)
        fencedCodeBlock(builder)
        indentedCodeBlock(builder)
        image(builder)
        bulletList(builder)
        orderedList(builder)
        listItem(builder)
        thematicBreak(builder)
        heading(builder)
        softLineBreak(builder)
        hardLineBreak(builder)
        paragraph(builder)
        link(builder)
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        // reuse this one for both code-blocks (indent & fenced)

        val codeBlockSpanFactory = CodeBlockSpanFactory()

        builder.setFactory(StrongEmphasis::class.java, StrongEmphasisSpanFactory()).setFactory(
            Emphasis::class.java, EmphasisSpanFactory()
        ).setFactory(BlockQuote::class.java, BlockQuoteSpanFactory()).setFactory(
            Code::class.java, CodeSpanFactory()
        ).setFactory(FencedCodeBlock::class.java, codeBlockSpanFactory)
            .setFactory(IndentedCodeBlock::class.java, codeBlockSpanFactory).setFactory(
                ListItem::class.java, ListItemSpanFactory()
            ).setFactory(Heading::class.java, HeadingSpanFactory()).setFactory(
                Link::class.java, LinkSpanFactory()
            ).setFactory(ThematicBreak::class.java, ThematicBreakSpanFactory())
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        measure(textView, markdown)

        // @since 4.4.0
        // we do not break API compatibility, instead we introduce the `instance of` check
        if (markdown is Spannable) {
            applyTo(markdown, textView)
        }
    }

    override fun afterSetText(textView: TextView) {
        // let's ensure that there is a movement method applied
        // we do it `afterSetText` so any user-defined movement method won't be
        // replaced (it should be done in `beforeSetText` or manually on a TextView)
        // @since 4.5.0 we additionally check if we should apply _implicit_ movement method
        if (!hasExplicitMovementMethod && textView.movementMethod == null) {
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun text(builder: MarkwonVisitor.Builder) {
        builder.on(
            Text::class.java, MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, text: Text ->
                val literal = text.literal
                visitor!!.builder().append(literal)

                // @since 4.0.0
                if (!onTextAddedListeners.isEmpty()) {
                    // calculate the start position
                    val length = visitor.length() - literal.length
                    for (onTextAddedListener in onTextAddedListeners) {
                        onTextAddedListener.onTextAdded(visitor, literal, length)
                    }
                }
            })
    }

    companion object {
        @JvmStatic
        fun create(): CorePlugin {
            return CorePlugin()
        }

        /**
         * @return a set with enabled by default block types
         * @since 4.4.0
         */
        fun enabledBlockTypes(): MutableSet<Class<out Block>> {
            return HashSet(
                listOf(
                    BlockQuote::class.java,
                    Heading::class.java,
                    FencedCodeBlock::class.java,
                    HtmlBlock::class.java,
                    ThematicBreak::class.java,
                    ListBlock::class.java,
                    IndentedCodeBlock::class.java
                )
            )
        }

        private fun strongEmphasis(builder: MarkwonVisitor.Builder) {
            builder.on(
                StrongEmphasis::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, strongEmphasis: StrongEmphasis ->
                    val length = visitor!!.length()
                    visitor.visitChildren(strongEmphasis)
                    visitor.setSpansForNodeOptional(strongEmphasis, length)
                })
        }

        private fun emphasis(builder: MarkwonVisitor.Builder) {
            builder.on(
                Emphasis::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, emphasis: Emphasis ->
                    val length = visitor!!.length()
                    visitor.visitChildren(emphasis)
                    visitor.setSpansForNodeOptional(emphasis, length)
                })
        }

        private fun blockQuote(builder: MarkwonVisitor.Builder) {
            builder.on(
                BlockQuote::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, blockQuote: BlockQuote ->
                    visitor!!.blockStart(blockQuote)
                    val length = visitor.length()

                    visitor.visitChildren(blockQuote)
                    visitor.setSpansForNodeOptional(blockQuote, length)
                    visitor.blockEnd(blockQuote)
                })
        }

        private fun code(builder: MarkwonVisitor.Builder) {
            builder.on(
                Code::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, code: Code ->
                    val length = visitor!!.length()
                    // NB, in order to provide a _padding_ feeling code is wrapped inside two unbreakable spaces
                    // unfortunately we cannot use this for multiline code as we cannot control where a new line break will be inserted
                    visitor.builder().append('\u00a0').append(code.literal).append('\u00a0')
                    visitor.setSpansForNodeOptional(code, length)
                })
        }

        private fun fencedCodeBlock(builder: MarkwonVisitor.Builder) {
            builder.on(
                FencedCodeBlock::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, fencedCodeBlock: FencedCodeBlock ->
                    visitCodeBlock(
                        visitor!!, fencedCodeBlock.info, fencedCodeBlock.literal, fencedCodeBlock
                    )
                })
        }

        private fun indentedCodeBlock(builder: MarkwonVisitor.Builder) {
            builder.on(
                IndentedCodeBlock::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, indentedCodeBlock: IndentedCodeBlock ->
                    visitCodeBlock(
                        visitor!!, null, indentedCodeBlock.literal, indentedCodeBlock
                    )
                })
        }

        // @since 4.0.0
        // his method is moved from ImagesPlugin. Alternative implementations must set SpanFactory
        // for Image node in order for this visitor to function
        private fun image(builder: MarkwonVisitor.Builder) {
            builder.on(
                Image::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, image: Image ->

                    // if there is no image spanFactory, ignore
                    val spanFactory =
                        visitor!!.configuration().spansFactory().get(Image::class.java)
                    if (spanFactory == null) {
                        visitor.visitChildren(image)
                        return@NodeVisitor
                    }

                    val length = visitor.length()

                    visitor.visitChildren(image)

                    // we must check if anything _was_ added, as we need at least one char to render
                    if (length == visitor.length()) {
                        visitor.builder().append('\uFFFC')
                    }

                    val configuration = visitor.configuration()

                    val parent = image.parent
                    val link = parent is Link

                    val destination =
                        configuration.imageDestinationProcessor().process(image.destination)

                    val props = visitor.renderProps()

                    // apply image properties
                    // Please note that we explicitly set IMAGE_SIZE to null as we do not clear
                    // properties after we applied span (we could though)
                    ImageProps.DESTINATION.set(props, destination)
                    ImageProps.REPLACEMENT_TEXT_IS_LINK.set(props, link)
                    ImageProps.IMAGE_SIZE.set(props, null)
                    visitor.setSpans(length, spanFactory.getSpans(configuration, props))
                })
        }

        @VisibleForTesting
        fun visitCodeBlock(visitor: MarkwonVisitor, info: String?, code: String, node: Node) {
            visitor.blockStart(node)

            val length = visitor.length()

            visitor.builder().append('\u00a0').append('\n')
                .append(visitor.configuration().syntaxHighlight().highlight(info, code))

            visitor.ensureNewLine()

            visitor.builder().append('\u00a0')

            // @since 4.1.1
            CoreProps.CODE_BLOCK_INFO.set(visitor.renderProps(), info)

            visitor.setSpansForNodeOptional(node, length)

            visitor.blockEnd(node)
        }

        private fun bulletList(builder: MarkwonVisitor.Builder) {
            builder.on(BulletList::class.java, SimpleBlockNodeVisitor())
        }

        private fun orderedList(builder: MarkwonVisitor.Builder) {
            builder.on(OrderedList::class.java, SimpleBlockNodeVisitor())
        }

        private fun listItem(builder: MarkwonVisitor.Builder) {
            builder.on(
                ListItem::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, listItem: ListItem ->
                    val length = visitor!!.length()
                    // it's important to visit children before applying render props (
                    // we can have nested children, who are list items also, thus they will
                    // override out props (if we set them before visiting children)
                    visitor.visitChildren(listItem)

                    val parent: Node? = listItem.parent
                    if (parent is OrderedList) {
                        val start = parent.startNumber

                        CoreProps.LIST_ITEM_TYPE.set(
                            visitor.renderProps(), CoreProps.ListItemType.ORDERED
                        )
                        CoreProps.ORDERED_LIST_ITEM_NUMBER.set(visitor.renderProps(), start)

                        // after we have visited the children increment start number
                        parent.startNumber = parent.startNumber + 1
                    } else {
                        CoreProps.LIST_ITEM_TYPE.set(
                            visitor.renderProps(), CoreProps.ListItemType.BULLET
                        )
                        CoreProps.BULLET_LIST_ITEM_LEVEL.set(
                            visitor.renderProps(), listLevel(listItem)
                        )
                    }

                    visitor.setSpansForNodeOptional(listItem, length)
                    if (visitor.hasNext(listItem)) {
                        visitor.ensureNewLine()
                    }
                })
        }

        private fun listLevel(node: Node): Int {
            var level = 0
            var parent = node.parent
            while (parent != null) {
                if (parent is ListItem) {
                    level += 1
                }
                parent = parent.parent
            }
            return level
        }

        private fun thematicBreak(builder: MarkwonVisitor.Builder) {
            builder.on(
                ThematicBreak::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, thematicBreak: ThematicBreak ->
                    visitor!!.blockStart(thematicBreak)
                    val length = visitor.length()

                    // without space it won't render
                    visitor.builder().append('\u00a0')

                    visitor.setSpansForNodeOptional(thematicBreak, length)
                    visitor.blockEnd(thematicBreak)
                })
        }

        private fun heading(builder: MarkwonVisitor.Builder) {
            builder.on(
                Heading::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, heading: Heading ->
                    visitor!!.blockStart(heading)
                    val length = visitor.length()
                    visitor.visitChildren(heading)

                    CoreProps.HEADING_LEVEL.set(visitor.renderProps(), heading.level)

                    visitor.setSpansForNodeOptional(heading, length)
                    visitor.blockEnd(heading)
                })
        }

        private fun softLineBreak(builder: MarkwonVisitor.Builder) {
            builder.on(
                SoftLineBreak::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, softLineBreak: SoftLineBreak ->
                    visitor!!.builder().append(' ')
                })
        }

        private fun hardLineBreak(builder: MarkwonVisitor.Builder) {
            builder.on(
                HardLineBreak::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, hardLineBreak: HardLineBreak -> visitor!!.ensureNewLine() })
        }

        private fun paragraph(builder: MarkwonVisitor.Builder) {
            builder.on(
                Paragraph::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, paragraph: Paragraph ->
                    val inTightList: Boolean = isInTightList(paragraph)
                    if (!inTightList) {
                        visitor!!.blockStart(paragraph)
                    }

                    val length = visitor!!.length()
                    visitor.visitChildren(paragraph)

                    CoreProps.PARAGRAPH_IS_IN_TIGHT_LIST.set(visitor.renderProps(), inTightList)

                    // @since 1.1.1 apply paragraph span
                    visitor.setSpansForNodeOptional(paragraph, length)
                    if (!inTightList) {
                        visitor.blockEnd(paragraph)
                    }
                })
        }

        private fun isInTightList(paragraph: Paragraph): Boolean {
            val parent: Node? = paragraph.parent
            if (parent != null) {
                val gramps = parent.parent
                if (gramps is ListBlock) {
                    return gramps.isTight
                }
            }
            return false
        }

        private fun link(builder: MarkwonVisitor.Builder) {
            builder.on(
                Link::class.java,
                MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor?, link: Link ->
                    val length = visitor!!.length()
                    visitor.visitChildren(link)

                    val destination = link.destination

                    CoreProps.LINK_DESTINATION.set(visitor.renderProps(), destination)
                    visitor.setSpansForNodeOptional(link, length)
                })
        }
    }
}
