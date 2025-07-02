package io.noties.markwon.image

import android.graphics.drawable.Drawable

abstract class AsyncDrawableLoader {
    /**
     * @since 4.0.0
     */
    abstract fun load(drawable: AsyncDrawable)

    /**
     * @since 4.0.0
     */
    abstract fun cancel(drawable: AsyncDrawable)

    abstract fun placeholder(drawable: AsyncDrawable): Drawable?

    companion object {
        /**
         * @since 3.0.0
         */
        @JvmStatic
        fun noOp(): AsyncDrawableLoader {
            return AsyncDrawableLoaderNoOp()
        }
    }
}
