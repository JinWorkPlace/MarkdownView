package io.noties.markwon.image

import android.graphics.drawable.Drawable
import io.noties.markwon.image.ImageItem.Companion.withDecodingNeeded
import io.noties.markwon.image.ImageItem.Companion.withResult
import java.io.InputStream

/**
 * @since 2.0.0
 */
abstract class ImageItem private constructor() {

    /**
     * Create an [ImageItem] with result, so no further decoding is required.
     *
     * @see withDecodingNeeded
     * @see WithResult
     * @since 4.0.0
     */
    companion object {
        fun withResult(drawable: Drawable): ImageItem {
            return WithResult(drawable)
        }

        /**
         * Create an [ImageItem] that requires further decoding of InputStream.
         *
         * @see withResult
         * @see WithDecodingNeeded
         * @since 4.0.0
         */
        fun withDecodingNeeded(contentType: String?, inputStream: InputStream): ImageItem {
            return WithDecodingNeeded(contentType, inputStream)
        }
    }

    /**
     * @since 4.0.0
     */
    abstract fun hasResult(): Boolean

    /**
     * @since 4.0.0
     */
    abstract fun hasDecodingNeeded(): Boolean

    /**
     * @see hasResult
     * @since 4.0.0
     */
    abstract fun getAsWithResult(): WithResult

    /**
     * @see hasDecodingNeeded
     * @since 4.0.0
     */
    abstract fun getAsWithDecodingNeeded(): WithDecodingNeeded

    /**
     * @since 4.0.0
     */
    class WithResult internal constructor(
        private val result: Drawable
    ) : ImageItem() {

        fun result(): Drawable = result

        override fun hasResult(): Boolean = true

        override fun hasDecodingNeeded(): Boolean = false

        override fun getAsWithResult(): WithResult = this

        override fun getAsWithDecodingNeeded(): WithDecodingNeeded {
            throw IllegalStateException()
        }
    }

    /**
     * @since 4.0.0
     */
    class WithDecodingNeeded internal constructor(
        private val contentType: String?, private val inputStream: InputStream
    ) : ImageItem() {

        fun contentType(): String? = contentType

        fun inputStream(): InputStream = inputStream

        override fun hasResult(): Boolean = false

        override fun hasDecodingNeeded(): Boolean = true

        override fun getAsWithResult(): WithResult {
            throw IllegalStateException()
        }

        override fun getAsWithDecodingNeeded(): WithDecodingNeeded = this
    }
}