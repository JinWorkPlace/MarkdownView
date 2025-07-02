package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.CoreProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import org.commonmark.node.ListItem

class ListHandler : TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    ) {
        if (!tag.isBlock) {
            return
        }

        val block = tag.asBlock
        val ol = "ol" == block.name()
        val ul = "ul" == block.name()

        if (!ol && !ul) {
            return
        }

        val configuration = visitor.configuration()
        val renderProps = visitor.renderProps()
        val spanFactory = configuration.spansFactory().get(ListItem::class.java)

        var number = 1
        val bulletLevel: Int = currentBulletListLevel(block)

        for (child in block.children()) {
            visitChildren(visitor, renderer, child)

            if (spanFactory != null && "li" == child.name()) {
                // insert list item here

                if (ol) {
                    CoreProps.LIST_ITEM_TYPE.set(renderProps, CoreProps.ListItemType.ORDERED)
                    CoreProps.ORDERED_LIST_ITEM_NUMBER.set(renderProps, number++)
                } else {
                    CoreProps.LIST_ITEM_TYPE.set(renderProps, CoreProps.ListItemType.BULLET)
                    CoreProps.BULLET_LIST_ITEM_LEVEL.set(renderProps, bulletLevel)
                }

                SpannableBuilder.setSpans(
                    visitor.builder(),
                    spanFactory.getSpans(configuration, renderProps),
                    child.start(),
                    child.end()
                )
            }
        }
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("ol", "ul")
    }

    companion object {
        private fun currentBulletListLevel(block: HtmlTag.Block): Int {
            var level = 0
            var currentBlock: HtmlTag.Block? = block
            while (currentBlock != null) {
                if (currentBlock.name() == "ul" || currentBlock.name() == "ol") {
                    level += 1
                }
                currentBlock = currentBlock.parent()
            }
            return level
        }
    }
}
