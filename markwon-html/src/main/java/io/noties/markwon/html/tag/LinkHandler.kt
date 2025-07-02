package io.noties.markwon.html.tag

import android.text.TextUtils
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.core.CoreProps
import io.noties.markwon.html.HtmlTag
import org.commonmark.node.Link

class LinkHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any? {
        val destination = tag.attributes()["href"]
        if (!TextUtils.isEmpty(destination)) {
            val spanFactory = configuration.spansFactory().get(Link::class.java)
            if (spanFactory != null) {
                CoreProps.LINK_DESTINATION.set(
                    renderProps, destination
                )

                return spanFactory.getSpans(configuration, renderProps)
            }
        }
        return null
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("a")
    }
}
