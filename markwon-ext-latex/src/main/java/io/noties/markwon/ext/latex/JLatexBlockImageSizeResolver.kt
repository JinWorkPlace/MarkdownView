package io.noties.markwon.ext.latex

import android.graphics.Rect
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImageSizeResolver

// we must make drawable fit canvas (if specified), but do not keep the ratio whilst scaling up
// @since 4.0.0
internal class JLatexBlockImageSizeResolver(
    private val fitCanvas: Boolean
) : ImageSizeResolver() {
    override fun resolveImageSize(drawable: AsyncDrawable): Rect {
        val imageBounds = drawable.result!!.bounds
        val canvasWidth = drawable.lastKnownCanvasWidth

        if (fitCanvas) {
            // we modify bounds only if `fitCanvas` is true

            val w = imageBounds.width()

            if (w < canvasWidth) {
                // increase width and center formula (keep height as-is)
                return Rect(0, 0, canvasWidth, imageBounds.height())
            }

            // @since 4.0.2 we additionally scale down the resulting formula (keeping the ratio)
            // the thing is - JLatexMathDrawable will do it anyway, but it will modify its own
            // bounds (which AsyncDrawable won't catch), thus leading to an empty space after the formula
            if (w > canvasWidth) {
                // here we must scale it down (keeping the ratio)
                val ratio = w.toFloat() / imageBounds.height()
                val h = (canvasWidth / ratio + .5f).toInt()
                return Rect(0, 0, canvasWidth, h)
            }
        }

        return imageBounds
    }
}
