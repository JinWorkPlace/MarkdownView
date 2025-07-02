package io.noties.markwon.html

/**
 * This class will be used to append some text to output in order to
 * apply a Span for this tag. Please note that this class will be used for
 * _void_ tags and tags that are self-closed (even if HTML spec doesn\'t specify
 * a tag as self-closed). This is due to the fact that underlying parser does not
 * validate context and does not check if a tag is correctly used. Plus it will be
 * used for tags without content, for example: `<my-custom-element></my-custom-element>`
 *
 * @since 2.0.0
 */
open class HtmlEmptyTagReplacement {
    /**
     * @return replacement for supplied startTag or null if no replacement should occur (which will
     * lead to `Inline` tag have start &amp; end the same value, thus not applicable for applying a Span)
     */
    open fun replace(tag: HtmlTag): String? {
        val replacement: String?

        val name = tag.name()

        when (name) {
            "br" -> replacement = "\n"
            "img" -> {
                val alt = tag.attributes()["alt"]
                replacement = if (alt == null || alt.isEmpty()) {
                    // no alt is provided
                    IMG_REPLACEMENT
                } else {
                    alt
                }
            }

            // @since 4.4.0 make iframe non-empty
            "iframe" -> replacement = IFRAME_REPLACEMENT

            else -> replacement = null
        }

        return replacement
    }

    companion object {
        @JvmStatic
        fun create(): HtmlEmptyTagReplacement {
            return HtmlEmptyTagReplacement()
        }

        private const val IMG_REPLACEMENT = "\uFFFC"
        private const val IFRAME_REPLACEMENT = "\u00a0" // non-breakable space
    }
}
