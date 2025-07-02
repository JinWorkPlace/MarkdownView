package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.core.CoreProps
import io.noties.markwon.html.HtmlTag
import org.commonmark.node.Heading

class HeadingHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any? {
        val factory = configuration.spansFactory().get(Heading::class.java)
        if (factory == null) {
            return null
        }

        var level: Int
        try {
            level = tag.name().substring(1).toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            level = 0
        }

        if (level < 1 || level > 6) {
            return null
        }

        CoreProps.HEADING_LEVEL.set(renderProps, level)

        return factory.getSpans(configuration, renderProps)
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("h1", "h2", "h3", "h4", "h5", "h6")
    }
}
