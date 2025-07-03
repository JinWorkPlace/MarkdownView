package io.noties.markwon.image.svg

import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import io.noties.markwon.image.MediaDecoder
import java.io.InputStream

/**
 * @since 4.2.0
 */
class SvgPictureMediaDecoder : MediaDecoder() {
    override fun decode(contentType: String?, inputStream: InputStream): Drawable {
        val svg: SVG
        try {
            svg = SVG.getFromInputStream(inputStream)
        } catch (e: SVGParseException) {
            throw IllegalStateException("Exception decoding SVG", e)
        }

        val picture = svg.renderToPicture()
        return PictureDrawable(picture)
    }

    override fun supportedTypes(): MutableCollection<String?> {
        return mutableSetOf(CONTENT_TYPE)
    }

    companion object {
        const val CONTENT_TYPE: String = "image/svg+xml"

        fun create(): SvgPictureMediaDecoder {
            return SvgPictureMediaDecoder()
        }
    }
}
