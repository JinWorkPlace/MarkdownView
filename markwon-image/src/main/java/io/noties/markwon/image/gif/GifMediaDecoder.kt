package io.noties.markwon.image.gif

import android.graphics.drawable.Drawable
import io.noties.markwon.image.MediaDecoder
import pl.droidsonroids.gif.GifDrawable
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @since 1.1.0
 */
open class GifMediaDecoder(private val autoPlayGif: Boolean) : MediaDecoder() {
    init {
        // @since 4.0.0
        validate()
    }

    override fun decode(contentType: String?, inputStream: InputStream): Drawable {
        val bytes: ByteArray
        try {
            bytes = readBytes(inputStream)
        } catch (e: IOException) {
            throw IllegalStateException("Cannot read GIF input-stream", e)
        }

        val drawable: GifDrawable
        try {
            drawable = newGifDrawable(bytes)
        } catch (e: IOException) {
            throw IllegalStateException("Exception creating GifDrawable", e)
        }

        if (!autoPlayGif) {
            drawable.pause()
        }

        return drawable
    }

    override fun supportedTypes(): MutableCollection<String?> {
        return mutableSetOf<String?>(CONTENT_TYPE)
    }

    @Throws(IOException::class)
    protected fun newGifDrawable(bytes: ByteArray): GifDrawable {
        return GifDrawable(bytes)
    }

    companion object {
        const val CONTENT_TYPE: String = "image/gif"

        /**
         * Creates a [GifMediaDecoder] with `autoPlayGif = true`
         *
         * @since 4.0.0
         */
        @JvmOverloads
        fun create(autoPlayGif: Boolean = true): GifMediaDecoder {
            return GifMediaDecoder(autoPlayGif)
        }

        @Throws(IOException::class)
        protected fun readBytes(stream: InputStream): ByteArray {
            val outputStream = ByteArrayOutputStream()
            val length = 1024 * 8
            val buffer = ByteArray(length)
            var read: Int
            while ((stream.read(buffer, 0, length).also { read = it }) != -1) {
                outputStream.write(buffer, 0, read)
            }
            return outputStream.toByteArray()
        }

        private fun validate() {
            check(GifSupport.hasGifSupport()) { GifSupport.missingMessage() }
        }
    }
}
