package io.noties.markwon.image

import android.graphics.drawable.Drawable
import java.io.InputStream

/**
 * @since 3.0.0
 */
abstract class MediaDecoder {
    /**
     * Changes since 4.0.0:
     *
     *  * Returns `non-null` drawable
     *  * Added `contentType` method parameter
     *
     */
    abstract fun decode(
        contentType: String?,
        inputStream: InputStream
    ): Drawable

    /**
     * @since 4.0.0
     */
    abstract fun supportedTypes(): MutableCollection<String?>
}
