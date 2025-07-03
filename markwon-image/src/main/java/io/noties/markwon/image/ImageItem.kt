package io.noties.markwon.image

import android.graphics.drawable.Drawable
import java.io.InputStream

/**
 * @since 2.0.0
 */
abstract class ImageItem private constructor() {
    /**
     * @since 4.0.0
     */
    abstract fun hasResult(): Boolean

    /**
     * @since 4.0.0
     */
    abstract fun hasDecodingNeeded(): Boolean

    /**
     * @see .hasResult
     * @since 4.0.0
     */
    abstract val asWithResult: WithResult

    /**
     * @see .hasDecodingNeeded
     * @since 4.0.0
     */
    abstract val asWithDecodingNeeded: WithDecodingNeeded

    /**
     * @since 4.0.0
     */
    class WithResult(private val result: Drawable) : ImageItem() {
        fun result(): Drawable {
            return result
        }

        override fun hasResult(): Boolean {
            return true
        }

        override fun hasDecodingNeeded(): Boolean {
            return false
        }

        override val asWithResult: WithResult
            get() = throw IllegalStateException()
        override val asWithDecodingNeeded: WithDecodingNeeded
            get() = throw IllegalStateException()

    }

    /**
     * @since 4.0.0
     */
    class WithDecodingNeeded(
        private val contentType: String?,
        private val inputStream: InputStream
    ) : ImageItem() {
        fun contentType(): String? {
            return contentType
        }

        fun inputStream(): InputStream {
            return inputStream
        }

        override fun hasResult(): Boolean {
            return false
        }

        override fun hasDecodingNeeded(): Boolean {
            return true
        }

        override val asWithResult: WithResult
            get() = throw IllegalStateException()
        override val asWithDecodingNeeded: WithDecodingNeeded
            get() = throw IllegalStateException()
    }

    companion object {
        /**
         * Create an [ImageItem] with result, so no further decoding is required.
         *
         * @see .withDecodingNeeded
         * @see WithResult
         *
         * @since 4.0.0
         */
        @JvmStatic
        fun withResult(drawable: Drawable): ImageItem {
            return WithResult(drawable)
        }

        /**
         * Create an [ImageItem] that requires further decoding of InputStream.
         *
         * @see .withResult
         * @see WithDecodingNeeded
         *
         * @since 4.0.0
         */
        @JvmStatic
        fun withDecodingNeeded(
            contentType: String?,
            inputStream: InputStream
        ): ImageItem {
            return WithDecodingNeeded(contentType, inputStream)
        }
    }
}
