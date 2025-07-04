package io.noties.markwon.image.gif

import android.util.Log

/**
 * @since 4.0.0
 */
object GifSupport {
    private val HAS_GIF: Boolean

    init {
        var result: Boolean
        try {
            // @since 4.3.1
            Class.forName("pl.droidsonroids.gif.GifDrawable")
            result = true
        } catch (_: Throwable) {
            // @since 4.1.1 instead of printing full stacktrace of the exception,
            // just print a warning to the console
            Log.w("MarkwonImagesPlugin", missingMessage())
            result = false
        }
        HAS_GIF = result
    }

    @JvmStatic
    fun hasGifSupport(): Boolean {
        return HAS_GIF
    }

    /**
     * @since 4.1.1
     */
    @JvmStatic
    fun missingMessage(): String {
        return "`pl.droidsonroids.gif:android-gif-drawable:*` " + "dependency is missing, please add to your project explicitly if you " + "wish to use GIF media-decoder"
    }
}
