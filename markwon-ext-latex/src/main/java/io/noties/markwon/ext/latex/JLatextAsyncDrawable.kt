package io.noties.markwon.ext.latex

import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolver

/**
 * @since 4.3.0
 */
class JLatextAsyncDrawable(
    destination: String,
    loader: AsyncDrawableLoader,
    imageSizeResolver: ImageSizeResolver,
    imageSize: ImageSize?,
    val isBlock: Boolean
) : AsyncDrawable(destination, loader, imageSizeResolver, imageSize)
