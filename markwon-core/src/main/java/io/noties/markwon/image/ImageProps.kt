package io.noties.markwon.image

import io.noties.markwon.Prop

/**
 * @since 3.0.0
 */
object ImageProps {
    @JvmField
    val DESTINATION: Prop<String> = Prop.of("image-destination")

    @JvmField
    val REPLACEMENT_TEXT_IS_LINK: Prop<Boolean> = Prop.of("image-replacement-text-is-link")

    @JvmField
    val IMAGE_SIZE: Prop<ImageSize> = Prop.of("image-size")
}
