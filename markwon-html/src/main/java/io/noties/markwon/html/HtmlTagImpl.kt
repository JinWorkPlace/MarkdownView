package io.noties.markwon.html

import java.util.Collections

abstract class HtmlTagImpl protected constructor(
    @JvmField val name: String, @JvmField val start: Int, val attributes: MutableMap<String, String>
) : HtmlTag {
    @JvmField
    var end: Int = HtmlTag.Companion.NO_END

    override fun name(): String {
        return name
    }

    override fun start(): Int {
        return start
    }

    override fun end(): Int {
        return end
    }

    override val isEmpty: Boolean
        get() = start == end

    override fun attributes(): MutableMap<String, String> {
        return attributes
    }

    override val isClosed: Boolean
        get() = end > HtmlTag.Companion.NO_END

    abstract fun closeAt(end: Int)


    class InlineImpl(
        name: String, start: Int, attributes: MutableMap<String, String>
    ) : HtmlTagImpl(name, start, attributes), HtmlTag.Inline {
        override fun closeAt(end: Int) {
            if (!isClosed) {
                super.end = end
            }
        }

        override fun toString(): String {
            return "InlineImpl{name='$name', start=$start, end=$end, attributes=$attributes}"
        }

        override val isInline: Boolean
            get() = true

        override val isBlock: Boolean
            get() = false

        override val asInline: HtmlTag.Inline
            get() = this

        override val asBlock: HtmlTag.Block
            get() {
                throw ClassCastException("Cannot cast Inline instance to Block")
            }
    }

    class BlockImpl(
        name: String,
        start: Int,
        attributes: MutableMap<String, String>,
        @JvmField val parent: BlockImpl?
    ) : HtmlTagImpl(name, start, attributes), HtmlTag.Block {
        @JvmField
        var children: MutableList<BlockImpl>? = null

        override fun closeAt(end: Int) {
            if (!isClosed) {
                super.end = end
                if (children != null) {
                    for (child in children) {
                        child.closeAt(end)
                    }
                }
            }
        }

        override val isRoot: Boolean
            get() = parent == null

        override fun parent(): HtmlTag.Block? {
            return parent
        }

        override fun children(): MutableList<HtmlTag.Block> {
            val list: MutableList<HtmlTag.Block>
            if (children == null) {
                list = mutableListOf()
            } else {
                list = Collections.unmodifiableList(children as MutableList<out HtmlTag.Block>)
            }
            return list
        }

        override fun attributes(): MutableMap<String, String> {
            return attributes
        }

        override val isInline: Boolean
            get() = false

        override val isBlock: Boolean
            get() = true

        override val asInline: HtmlTag.Inline
            get() {
                throw ClassCastException("Cannot cast Block instance to Inline")
            }

        override val asBlock: HtmlTag.Block
            get() = this

        override fun toString(): String {
            return "BlockImpl{" + "name='" + name + '\'' + ", start=" + start + ", end=" + end + ", attributes=" + attributes + ", parent=" + (parent?.name) + ", children=" + children + '}'
        }

        companion object {
            @JvmStatic
            fun root(): BlockImpl {
                return BlockImpl("", 0, mutableMapOf(), null)
            }

            fun create(
                name: String, start: Int, attributes: MutableMap<String, String>, parent: BlockImpl?
            ): BlockImpl {
                return BlockImpl(name, start, attributes, parent)
            }
        }
    }
}
