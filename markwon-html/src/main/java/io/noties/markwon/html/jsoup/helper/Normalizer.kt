package io.noties.markwon.html.jsoup.helper

/**
 * Util methods for normalizing strings. Jsoup internal use only, please don't depend on this API.
 */
object Normalizer {
    @JvmStatic
    fun lowerCase(input: String?): String {
        return input?.lowercase() ?: ""
    }

    fun normalize(input: String?): String {
        return lowerCase(input).trim { it <= ' ' }
    }
}

