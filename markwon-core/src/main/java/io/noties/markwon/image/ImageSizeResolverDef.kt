package io.noties.markwon.image

import android.graphics.Rect

/**
 * @since 1.0.1
 */
@Suppress("unused")
open class ImageSizeResolverDef : ImageSizeResolver() {
    override fun resolveImageSize(drawable: AsyncDrawable): Rect {
        return resolveImageSize(
            drawable.imageSize,
            drawable.result.bounds,
            drawable.lastKnownCanvasWidth,
            drawable.lastKnowTextSize
        )
    }

    fun resolveImageSize(
        imageSize: ImageSize?, imageBounds: Rect, canvasWidth: Int, textSize: Float
    ): Rect {
        if (imageSize == null) {
            // @since 2.0.0 post process bounds to fit canvasWidth (previously was inside AsyncDrawable)
            //      must be applied only if imageSize is null
            val rect: Rect
            val w = imageBounds.width()
            if (w > canvasWidth) {
                val reduceRatio = w.toFloat() / canvasWidth
                rect = Rect(
                    0, 0, canvasWidth, (imageBounds.height() / reduceRatio + .5f).toInt()
                )
            } else {
                rect = imageBounds
            }
            return rect
        }

        val rect: Rect

        val width = imageSize.width
        val height: ImageSize.Dimension? = imageSize.height

        val imageWidth = imageBounds.width()
        val imageHeight = imageBounds.height()

        val ratio = imageWidth.toFloat() / imageHeight

        val w: Int = if (UNIT_PERCENT == width.unit) {
            (canvasWidth * (width.value / 100f) + .5f).toInt()
        } else {
            resolveAbsolute(width, imageWidth, textSize)
        }

        val h: Int = if (height == null || UNIT_PERCENT == height.unit) {
            (w / ratio + .5f).toInt()
        } else {
            resolveAbsolute(height, imageHeight, textSize)
        }

        rect = Rect(0, 0, w, h)

        return rect
    }

    protected fun resolveAbsolute(
        dimension: ImageSize.Dimension, original: Int, textSize: Float
    ): Int {
        val out: Int = if (UNIT_EM == dimension.unit) {
            (dimension.value * textSize + .5f).toInt()
        } else {
            (dimension.value + .5f).toInt()
        }
        return out
    }

    companion object {
        // we track these two, others are considered to be pixels
        const val UNIT_PERCENT: String = "%"
        const val UNIT_EM: String = "em"
    }
}
