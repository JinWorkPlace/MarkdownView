package io.noties.markwon.core

import io.noties.markwon.Prop

/**
 * @since 3.0.0
 */
object CoreProps {
    @JvmField
    val LIST_ITEM_TYPE: Prop<ListItemType> = Prop.of("list-item-type")

    @JvmField
    val BULLET_LIST_ITEM_LEVEL: Prop<Int> = Prop.of("bullet-list-item-level")

    @JvmField
    val ORDERED_LIST_ITEM_NUMBER: Prop<Int> = Prop.of("ordered-list-item-number")

    @JvmField
    val HEADING_LEVEL: Prop<Int> = Prop.of("heading-level")

    @JvmField
    val LINK_DESTINATION: Prop<String> = Prop.of("link-destination")

    @JvmField
    val PARAGRAPH_IS_IN_TIGHT_LIST: Prop<Boolean> = Prop.of("paragraph-is-in-tight-list")

    /**
     * @since 4.1.1
     */
    @JvmField
    val CODE_BLOCK_INFO: Prop<String> = Prop.of("code-block-info")

    enum class ListItemType {
        BULLET, ORDERED
    }
}
