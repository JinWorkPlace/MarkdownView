package io.noties.markwon.image

import android.net.Uri

/**
 * @since 3.0.0
 */
abstract class SchemeHandler {
    /**
     * Changes since 4.0.0:
     *
     *  * Returns `non-null` image-item
     *
     *
     * @see ImageItem.withResult
     * @see ImageItem.withDecodingNeeded
     */
    abstract fun handle(raw: String, uri: Uri): ImageItem

    /**
     * @since 4.0.0
     */
    abstract fun supportedSchemes(): MutableCollection<String>
}
