package io.noties.markwon.html

import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.MarkwonHtmlParser.FlushAction
import java.util.Collections

internal class MarkwonHtmlRendererImpl(
    private val allowNonClosedTags: Boolean, private val tagHandlers: MutableMap<String, TagHandler>
) : MarkwonHtmlRenderer() {
    override fun render(visitor: MarkwonVisitor, parser: MarkwonHtmlParser) {
        val end: Int = if (!allowNonClosedTags) {
            HtmlTag.NO_END
        } else {
            visitor.length()
        }

        parser.flushInlineTags(end) { tags: MutableList<HtmlTag.Inline> ->
            var handler: TagHandler?
            for (inline in tags) {
                // if tag is not closed -> do not render

                if (!inline.isClosed) {
                    continue
                }

                handler = tagHandler(inline.name())
                handler?.handle(visitor, this@MarkwonHtmlRendererImpl, inline)
            }
        }

        parser.flushBlockTags(end, object : FlushAction<HtmlTag.Block> {
            override fun apply(tags: MutableList<HtmlTag.Block>) {
                var handler: TagHandler?

                for (block in tags) {
                    if (!block.isClosed) {
                        continue
                    }

                    handler = tagHandler(block.name())
                    if (handler != null) {
                        handler.handle(visitor, this@MarkwonHtmlRendererImpl, block)
                    } else {
                        // see if any of children can be handled
                        apply(block.children())
                    }
                }
            }
        })

        parser.reset()
    }

    override fun tagHandler(tagName: String): TagHandler? {
        return tagHandlers[tagName]
    }

    internal class Builder {
        private val tagHandlers: MutableMap<String, TagHandler> = HashMap(2)
        private var allowNonClosedTags = false
        private var excludeDefaults = false

        private var isBuilt = false

        fun allowNonClosedTags(allowNonClosedTags: Boolean) {
            checkState()
            this.allowNonClosedTags = allowNonClosedTags
        }

        fun addHandler(tagHandler: TagHandler) {
            checkState()
            for (tag in tagHandler.supportedTags()) {
                tagHandlers.put(tag, tagHandler)
            }
        }

        fun getHandler(tagName: String): TagHandler? {
            checkState()
            return tagHandlers[tagName]
        }

        fun excludeDefaults(excludeDefaults: Boolean) {
            checkState()
            this.excludeDefaults = excludeDefaults
        }

        fun excludeDefaults(): Boolean {
            return excludeDefaults
        }

        fun build(): MarkwonHtmlRenderer {
            checkState()

            isBuilt = true

            // okay, let's validate that we have at least one tagHandler registered
            // if we have none -> return no-op implementation
            return if (!tagHandlers.isEmpty()) MarkwonHtmlRendererImpl(
                allowNonClosedTags, Collections.unmodifiableMap(tagHandlers)
            ) else MarkwonHtmlRendererNoOp()
        }

        private fun checkState() {
            check(!isBuilt) { "Builder has been already built" }
        }

        fun addDefaultTagHandler(tagHandler: TagHandler) {
            for (tag in tagHandler.supportedTags()) {
                if (!tagHandlers.containsKey(tag)) {
                    tagHandlers.put(tag, tagHandler)
                }
            }
        }
    }
}
