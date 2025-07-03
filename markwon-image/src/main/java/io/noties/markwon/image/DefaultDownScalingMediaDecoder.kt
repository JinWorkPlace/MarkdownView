package io.noties.markwon.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min
import androidx.core.graphics.drawable.toDrawable

/**
 * A [MediaDecoder] that additionally process media resource to optionally
 * scale it down to fit specified maximum values. Should be used to ensure that no exception is raised
 * whilst rendering (`Canvas: trying to draw too large(Xbytes) bitmap`) or `OutOfMemoryException` is thrown.
 *
 * **NB** this media decoder will create a temporary file for each incoming media resource,
 * which can have a performance penalty (IO)
 *
 * @since 4.6.2
 */
class DefaultDownScalingMediaDecoder private constructor(
    private val resources: Resources,
    private val maxWidth: Int,
    private val maxHeight: Int
) : MediaDecoder() {
    // https://android.jlelse.eu/loading-large-bitmaps-efficiently-in-android-66826cd4ad53
    override fun decode(contentType: String?, inputStream: InputStream): Drawable {
        val file: File = writeToTempFile(inputStream)
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            // initial result when obtaining bounds is discarded
            decode(file, options)

            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            val bitmap: Bitmap? = decode(file, options)

            return bitmap!!.toDrawable(resources)
        } finally {
            // we no longer need the temporary file
            file.delete()
            Log.e("DefaultDownScalingMediaDecoder", "decode error", )
        }
    }

    override fun supportedTypes(): MutableCollection<String?> {
        return mutableSetOf<String?>()
    }

    companion object {
        /**
         * Values `<= 0` are ignored, a dimension is considered to be not restrained any limit in such case
         */
        fun create(maxWidth: Int, maxHeight: Int): DefaultDownScalingMediaDecoder {
            return create(Resources.getSystem(), maxWidth, maxHeight)
        }

        fun create(
            resources: Resources,
            maxWidth: Int,
            maxHeight: Int
        ): DefaultDownScalingMediaDecoder {
            return DefaultDownScalingMediaDecoder(resources, maxWidth, maxHeight)
        }

        private fun writeToTempFile(inputStream: InputStream): File {
            val file: File
            try {
                file = File.createTempFile("markwon", null)
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }

            val outputStream: OutputStream
            try {
                outputStream = BufferedOutputStream(FileOutputStream(file, false))
            } catch (e: FileNotFoundException) {
                throw IllegalStateException(e)
            }

            val buffer = ByteArray(1024 * 8)
            var length: Int
            try {
                while ((inputStream.read(buffer).also { length = it }) > 0) {
                    outputStream.write(buffer, 0, length)
                }
            } catch (e: IOException) {
                throw IllegalStateException(e)
            } finally {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    throw (e)
                    // ignored
                }
            }

            return file
        }

        private fun decode(file: File, options: BitmapFactory.Options): Bitmap? {
            val `is`: InputStream = readFile(file)
            // not yet, still min SDK is 16
            try {
                return BitmapFactory.decodeStream(`is`, null, options)
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    // ignored

                }
            }
        }


        private fun readFile(file: File): InputStream {
            try {
                return BufferedInputStream(FileInputStream(file))
            } catch (e: FileNotFoundException) {
                throw IllegalStateException(e)
            }
        }

        // see: https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            maxWidth: Int,
            maxHeight: Int
        ): Int {
            val w = options.outWidth
            val h = options.outHeight

            val hasMaxWidth = maxWidth > 0
            val hasMaxHeight = maxHeight > 0

            val inSampleSize: Int
            if (hasMaxWidth && hasMaxHeight) {
                // minimum of both
                inSampleSize =
                    min(calculateInSampleSize(w, maxWidth), calculateInSampleSize(h, maxHeight))
            } else if (hasMaxWidth) {
                inSampleSize = calculateInSampleSize(w, maxWidth)
            } else if (hasMaxHeight) {
                inSampleSize = calculateInSampleSize(h, maxHeight)
            } else {
                // else no sampling, as we have no dimensions to base our calculations on
                inSampleSize = 1
            }

            return inSampleSize
        }

        private fun calculateInSampleSize(actual: Int, max: Int): Int {
            var inSampleSize = 1
            val half = actual / 2
            while ((half / inSampleSize) > max) {
                inSampleSize *= 2
            }
            return inSampleSize
        }
    }
}
