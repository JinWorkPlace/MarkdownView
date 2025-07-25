package io.noties.markwon.linkify

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.annotation.IntDef
import androidx.core.text.util.LinkifyCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CorePlugin.OnTextAddedListener
import io.noties.markwon.core.CoreProps
import org.commonmark.node.Link

class LinkifyPlugin internal constructor(
    @param:LinkifyMask private val mask: Int, private val useCompat: Boolean
) : AbstractMarkwonPlugin() {
    @IntDef(flag = true, value = [Linkify.EMAIL_ADDRESSES, Linkify.PHONE_NUMBERS, Linkify.WEB_URLS])
    @Retention(
        AnnotationRetention.SOURCE
    )
    internal annotation class LinkifyMask

    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(
            CorePlugin::class.java, MarkwonPlugin.Action { corePlugin ->
                // @since 4.3.0
                val listener: LinkifyTextAddedListener = if (useCompat) {
                    LinkifyCompatTextAddedListener(mask)
                } else {
                    LinkifyTextAddedListener(mask)
                }
                corePlugin.addOnTextAddedListener(listener)
            })
    }

    private open class LinkifyTextAddedListener(private val mask: Int) : OnTextAddedListener {
        override fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int) {
            // @since 4.2.0 obtain span factory for links
            //  we will be using the link that is used by markdown (instead of directly applying URLSpan)

            val spanFactory = visitor.configuration().spansFactory().get(Link::class.java)
            if (spanFactory == null) {
                return
            }

            // @since 4.2.0 we no longer re-use builder (thread safety achieved for
            //  render calls from different threads and ... better performance)
            val builder = SpannableStringBuilder(text)

            if (addLinks(builder, mask)) {
                // target URL span specifically
                val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
                if (spans != null && spans.size > 0) {
                    val renderProps = visitor.renderProps()
                    val spannableBuilder = visitor.builder()

                    for (span in spans) {
                        if (span != null) {
                            CoreProps.LINK_DESTINATION.set(renderProps, span.url)
                        }
                        SpannableBuilder.setSpans(
                            spannableBuilder,
                            spanFactory.getSpans(visitor.configuration(), renderProps),
                            start + builder.getSpanStart(span),
                            start + builder.getSpanEnd(span)
                        )
                    }
                }
            }
        }

        protected open fun addLinks(text: Spannable, @LinkifyMask mask: Int): Boolean {
            return Linkify.addLinks(text, mask)
        }
    }

    // @since 4.3.0
    private class LinkifyCompatTextAddedListener(mask: Int) : LinkifyTextAddedListener(mask) {
        override fun addLinks(text: Spannable, @LinkifyMask mask: Int): Boolean {
            return LinkifyCompat.addLinks(text, mask)
        }
    }

    companion object {
        /**
         * @param useCompat If true, use [LinkifyCompat] to handle links.
         * Note that the [LinkifyCompat] depends on androidx.core:core,
         * the dependency must be added on a client side explicitly.
         * @since 4.3.0 `useCompat` argument
         */
        @JvmOverloads
        fun create(useCompat: Boolean = false): LinkifyPlugin {
            return create(
                Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS, useCompat
            )
        }

        fun create(@LinkifyMask mask: Int): LinkifyPlugin {
            return LinkifyPlugin(mask, false)
        }

        /**
         * @param useCompat If true, use [LinkifyCompat] to handle links.
         * Note that the [LinkifyCompat] depends on androidx.core:core,
         * the dependency must be added on a client side explicitly.
         * @since 4.3.0 `useCompat` argument
         */
        fun create(@LinkifyMask mask: Int, useCompat: Boolean): LinkifyPlugin {
            return LinkifyPlugin(mask, useCompat)
        }
    }
}
