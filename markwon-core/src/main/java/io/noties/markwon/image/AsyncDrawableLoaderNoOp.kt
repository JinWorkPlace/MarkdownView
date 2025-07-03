package io.noties.markwon.image

import android.graphics.drawable.Drawable

internal class AsyncDrawableLoaderNoOp : AsyncDrawableLoader() {
    override fun load(drawable: AsyncDrawable) {
    }

    override fun cancel(drawable: AsyncDrawable) {
    }

    override fun placeholder(drawable: AsyncDrawable): Drawable? {
        return null
    }
}
