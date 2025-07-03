package io.noties.markwon.image.destination

import android.text.TextUtils
import androidx.core.net.toUri

/**
 * [ImageDestinationProcessor] that treats all destinations **without scheme**
 * information as pointing to the `assets` folder of an application. Please note that this
 * processor only adds required `file:///android_asset/` prefix to destinations and
 * actual image loading must take that into account (implement this functionality).
 *
 *
 * `FileSchemeHandler` from the `image` module supports asset images when created with
 * `createWithAssets` factory method
 *
 * @since 4.4.0
 */
class ImageDestinationProcessorAssets @JvmOverloads constructor(
    private val processor: ImageDestinationProcessor? = null
) : ImageDestinationProcessor() {
    private val assetsProcessor: ImageDestinationProcessorRelativeToAbsolute =
        ImageDestinationProcessorRelativeToAbsolute(MOCK)

    override fun process(destination: String): String {
        val out: String
        val uri = destination.toUri()
        (if (TextUtils.isEmpty(uri.scheme)) {
            assetsProcessor.process(destination).replace(MOCK, BASE)
        } else {
            processor?.process(destination) ?: destination
        }).also { out = it }
        return out
    }

    companion object {
        fun create(parent: ImageDestinationProcessor): ImageDestinationProcessorAssets {
            return ImageDestinationProcessorAssets(parent)
        }

        const val MOCK: String = "https://android.asset/"
        const val BASE: String = "file:///android_asset/"
    }
}
