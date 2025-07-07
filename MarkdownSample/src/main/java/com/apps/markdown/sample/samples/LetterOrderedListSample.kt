package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.Prop
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.spans.BulletListItemSpan
import io.noties.markwon.core.spans.OrderedListItemSpan
import org.commonmark.node.BulletList
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList

@MarkwonSampleInfo(
    id = "20200629130954",
    title = "Letter ordered list",
    description = "Render bullet list inside an ordered list with letters instead of bullets",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING, Tag.PLUGIN, Tag.LISTS]
)
class LetterOrderedListSample : MarkwonTextViewSample() {
    override fun render() {
        // bullet list nested in ordered list renders letters instead of bullets
        val md =
            "" + "1. Hello there!\n" + "1. And here is how:\n" + "   - First\n" + "   - Second\n" + "   - Third\n" + "      1. And first here\n\n"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(BulletListIsOrderedWithLettersWhenNestedPlugin())
                .build()

        markwon.setMarkdown(textView, md)
    }
}

internal class BulletListIsOrderedWithLettersWhenNestedPlugin : AbstractMarkwonPlugin() {
    // or introduce some kind of synchronization if planning to use from multiple threads,
    //  for example via ThreadLocal
    private val bulletCounter = android.util.SparseIntArray()

    override fun afterRender(node: Node, visitor: MarkwonVisitor) {
        // clear counter after render
        bulletCounter.clear()
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        // NB that both ordered and bullet lists are represented
        //  by ListItem (must inspect parent to detect the type)
        builder.on(
            ListItem::class.java,
            MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor, listItem: ListItem ->
                // mimic original behaviour (copy-pasta from CorePlugin)
                val length: Int = visitor.length()

                visitor.visitChildren(listItem)

                val parent: Node = listItem.parent
                if (parent is OrderedList) {
                    val start: Int = parent.startNumber

                    CoreProps.LIST_ITEM_TYPE.set(
                        visitor.renderProps(), CoreProps.ListItemType.ORDERED
                    )
                    CoreProps.ORDERED_LIST_ITEM_NUMBER.set(visitor.renderProps(), start)

                    // after we have visited the children increment start number
                    val orderedList: OrderedList = parent
                    orderedList.startNumber = orderedList.startNumber + 1
                } else {
                    CoreProps.LIST_ITEM_TYPE.set(
                        visitor.renderProps(), CoreProps.ListItemType.BULLET
                    )

                    if (isBulletOrdered(
                            parent
                        )
                    ) {
                        // obtain current count value
                        val count = currentBulletCountIn(parent)
                        BULLET_LETTER.set(
                            visitor.renderProps(), createBulletLetter(
                                count
                            )
                        )
                        // update current count value
                        setCurrentBulletCountIn(parent, count + 1)
                    } else {
                        CoreProps.BULLET_LIST_ITEM_LEVEL.set(
                            visitor.renderProps(), listLevel(
                                listItem
                            )
                        )
                        // clear letter info when regular bullet list is used
                        BULLET_LETTER.clear(
                            visitor.renderProps()
                        )
                    }
                }

                visitor.setSpansForNodeOptional(listItem, length)
                if (visitor.hasNext(listItem)) {
                    visitor.ensureNewLine()
                }
            })
    }

    override fun configureSpansFactory(builder: io.noties.markwon.MarkwonSpansFactory.Builder) {
        builder.setFactory(
            ListItem::class.java,
            object : SpanFactory {
                override fun getSpans(
                    configuration: MarkwonConfiguration, props: RenderProps
                ): Any {
                    return if (CoreProps.ListItemType.BULLET == CoreProps.LIST_ITEM_TYPE.require(
                            props
                        )
                    ) {
                        val letter: String = BULLET_LETTER.get(props)

                        if (!android.text.TextUtils.isEmpty(letter)) {
                            OrderedListItemSpan(configuration.theme(), letter)
                        } else {
                            BulletListItemSpan(
                                configuration.theme(),
                                CoreProps.BULLET_LIST_ITEM_LEVEL.require(props)
                            )
                        }
                    } else {
                        val number = CoreProps.ORDERED_LIST_ITEM_NUMBER.require(props)
                            .toString() + "." + '\u00a0'

                        OrderedListItemSpan(configuration.theme(), number)
                    }
                }
            },
        )
    }

    private fun currentBulletCountIn(parent: Node): Int {
        return bulletCounter.get(parent.hashCode(), 0)
    }

    private fun setCurrentBulletCountIn(parent: Node, count: Int) {
        bulletCounter.put(parent.hashCode(), count)
    }

    companion object {
        private val BULLET_LETTER = Prop.of<String>("my-bullet-letter")

        private fun createBulletLetter(count: Int): String {
            // or lower `a`
            // `'u00a0` is non-breakable space char
            return (('A'.code + count).toChar()).toString() + ".\u00a0"
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

        private fun isBulletOrdered(node: Node): Boolean {
            var node = node
            node = node.parent
            while (true) {
                if (node is OrderedList) {
                    return true
                }

                if (node is BulletList) {
                    return false
                }

                node = node.parent
            }
        }
    }
}
