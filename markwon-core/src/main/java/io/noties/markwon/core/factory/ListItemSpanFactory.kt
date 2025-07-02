package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.spans.BulletListItemSpan
import io.noties.markwon.core.spans.OrderedListItemSpan

class ListItemSpanFactory : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): Any {
        // type of list item
        // bullet : level
        // ordered: number

        val spans: Any

        if (CoreProps.ListItemType.BULLET == CoreProps.LIST_ITEM_TYPE.require(props)) {
            spans = BulletListItemSpan(
                configuration.theme(), CoreProps.BULLET_LIST_ITEM_LEVEL.require(props)
            )
        } else {
            val number =
                (CoreProps.ORDERED_LIST_ITEM_NUMBER.require(props).toString() + "." + '\u00a0')

            spans = OrderedListItemSpan(
                configuration.theme(), number
            )
        }

        return spans
    }
}
