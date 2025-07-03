package io.noties.markwon.image

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory

class ImageSpanFactory : SpanFactory {
    override fun getSpans(
        configuration: MarkwonConfiguration, props: RenderProps
    ): AsyncDrawableSpan {
        return AsyncDrawableSpan(
            configuration.theme(), AsyncDrawable(
                ImageProps.DESTINATION.require(props),
                configuration.asyncDrawableLoader(),
                configuration.imageSizeResolver(),
                ImageProps.IMAGE_SIZE.get(props)
            ), AsyncDrawableSpan.ALIGN_BOTTOM, ImageProps.REPLACEMENT_TEXT_IS_LINK.get(props, false)
        )
    }
}
