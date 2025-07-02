package io.noties.markwon.image

import android.graphics.Rect

/**
 * @see ImageSizeResolverDef
 *
 * @see io.noties.markwon.MarkwonConfiguration.Builder.imageSizeResolver
 * @since 1.0.1
 */
abstract class ImageSizeResolver {
    /**
     * @since 4.0.0
     */
    abstract fun resolveImageSize(drawable: AsyncDrawable): Rect
}
