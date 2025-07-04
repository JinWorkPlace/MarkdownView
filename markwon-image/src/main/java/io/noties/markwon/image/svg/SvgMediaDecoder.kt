package io.noties.markwon.image.svg

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import io.noties.markwon.image.MediaDecoder
import io.noties.markwon.image.svg.SvgSupport.hasSvgSupport
import io.noties.markwon.image.svg.SvgSupport.missingMessage
import java.io.InputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

/**
 * @since 1.1.0
 */
class SvgMediaDecoder internal constructor(
    private val resources: Resources
) : MediaDecoder() {
    init {
        validate()
    }

    override fun decode(contentType: String?, inputStream: InputStream): Drawable {
        val svg: SVG
        try {
            svg = SVG.getFromInputStream(inputStream)
        } catch (e: SVGParseException) {
            throw IllegalStateException("Exception decoding SVG", e)
        }

        val w = svg.getDocumentWidth()
        val h = svg.getDocumentHeight()
        val density = resources.displayMetrics.density

        val width = (w * density + .5f).toInt()
        val height = (h * density + .5f).toInt()

        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)
        svg.renderToCanvas(canvas)

        return bitmap.toDrawable(resources)
    }

    override fun supportedTypes(): MutableCollection<String> {
        return mutableSetOf(CONTENT_TYPE)
    }

    companion object {
        const val CONTENT_TYPE: String = "image/svg+xml"

        /**
         * @see .create
         * @since 4.0.0
         */
        @JvmOverloads
        fun create(resources: Resources = Resources.getSystem()): SvgMediaDecoder {
            return SvgMediaDecoder(resources)
        }

        private fun validate() {
            check(hasSvgSupport()) { missingMessage() }
        }
    }
}
