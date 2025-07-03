package io.noties.markwon.image.data

import android.net.Uri
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.SchemeHandler
import java.io.ByteArrayInputStream

/**
 * @since 2.0.0
 */
class DataUriSchemeHandler internal constructor(
    private val uriParser: DataUriParser,
    private val uriDecoder: DataUriDecoder
) : SchemeHandler() {
    override fun handle(raw: String, uri: Uri): ImageItem {
        check(raw.startsWith(START)) { "Invalid data-uri: " + raw }

        val part: String = raw.substring(START.length)

        val dataUri = uriParser.parse(part)
        checkNotNull(dataUri) { "Invalid data-uri: " + raw }

        val bytes: ByteArray?
        try {
            bytes = uriDecoder.decode(dataUri)
        } catch (t: Throwable) {
            throw IllegalStateException("Cannot decode data-uri: " + raw, t)
        }

        checkNotNull(bytes) { "Decoding data-uri failed: " + raw }

        return ImageItem.withDecodingNeeded(
            dataUri.contentType,
            ByteArrayInputStream(bytes)
        )
    }

    override fun supportedSchemes(): MutableCollection<String?> {
        return mutableSetOf<String?>(SCHEME)
    }

    companion object {
        const val SCHEME: String = "data"

        @JvmStatic
        fun create(): DataUriSchemeHandler {
            return DataUriSchemeHandler(DataUriParser.create(), DataUriDecoder.create())
        }

        private const val START = "data:"
    }
}
