package io.noties.markwon.image.svg

import android.util.Log

/**
 * @since 4.0.0
 */
object SvgSupport {
    private val HAS_SVG: Boolean

    init {
        var result: Boolean
        try {
            Class.forName("com.caverock.androidsvg.SVG")
            result = true
        } catch (t: Throwable) {
            // @since 4.1.1 instead of printing full stacktrace of the exception,
            // just print a warning to the console
            Log.w("MarkwonImagesPlugin", missingMessage())
            result = false
        }
        HAS_SVG = result
    }

    @JvmStatic
    fun hasSvgSupport(): Boolean {
        return HAS_SVG
    }

    /**
     * @since 4.1.1
     */
    fun missingMessage(): String {
        return "`com.caverock:androidsvg:*` dependency is missing, " +
                "please add to your project explicitly if you wish to use SVG media-decoder"
    }
}
