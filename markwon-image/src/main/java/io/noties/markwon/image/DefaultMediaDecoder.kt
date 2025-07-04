package io.noties.markwon.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import java.io.InputStream

/**
 * This class can be used as the last [MediaDecoder] to _try_ to handle all rest cases.
 * Here we just assume that supplied InputStream is of image type and try to decode it.
 *
 * **NB** if you are dealing with big images that require down scaling see [DefaultDownScalingMediaDecoder]
 * which additionally down scales displayed images.
 *
 * @see DefaultDownScalingMediaDecoder
 *
 * @since 1.1.0
 */
class DefaultMediaDecoder internal constructor(
    private val resources: Resources
) : MediaDecoder() {
    override fun decode(contentType: String?, inputStream: InputStream): Drawable {
        val bitmap: Bitmap?
        try {
            // absolutely not optimal... thing
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (t: Throwable) {
            throw IllegalStateException("Exception decoding input-stream", t)
        }

        return bitmap.toDrawable(resources)
    }

    override fun supportedTypes(): MutableCollection<String> {
        return mutableSetOf()
    }

    companion object {
        @JvmStatic
        fun create(): DefaultMediaDecoder {
            return DefaultMediaDecoder(Resources.getSystem())
        }

        fun create(resources: Resources): DefaultMediaDecoder {
            return DefaultMediaDecoder(resources)
        }
    }
}
