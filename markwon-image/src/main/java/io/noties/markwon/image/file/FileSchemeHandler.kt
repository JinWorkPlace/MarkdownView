package io.noties.markwon.image.file

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.SchemeHandler
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * @since 3.0.0
 */
class FileSchemeHandler internal constructor(
    private val assetManager: AssetManager?
) : SchemeHandler() {
    override fun handle(raw: String, uri: Uri): ImageItem {
        val segments = uri.pathSegments
        check(
            !(segments == null || segments.isEmpty())
        ) { "Invalid file path: $raw" }

        val inputStream: InputStream

        val assets = FILE_ANDROID_ASSETS == segments[0]
        val fileName = uri.lastPathSegment

        if (assets) {
            // no handling of assets here if we have no assetsManager

            if (assetManager != null) {
                val path = StringBuilder()
                var i = 1
                val size = segments.size
                while (i < size) {
                    if (i != 1) {
                        path.append('/')
                    }
                    path.append(segments.get(i))
                    i++
                }

                // load assets
                try {
                    inputStream = assetManager.open(path.toString())
                } catch (e: IOException) {
                    throw IllegalStateException(
                        "Exception obtaining asset file: $raw, path: $path", e
                    )
                }
            } else {
                throw IllegalStateException(
                    "Supplied file path points to assets, " + "but FileSchemeHandler was not supplied with AssetsManager. " + "Use `#createWithAssets` factory method to create FileSchemeHandler " + "that can handle android assets"
                )
            }
        } else {
            val path = uri.path ?: ""
            check(!TextUtils.isEmpty(path)) { "Invalid file path: $raw, $path" }

            try {
                inputStream = BufferedInputStream(FileInputStream(File(path)))
            } catch (e: FileNotFoundException) {
                throw IllegalStateException("Exception reading file: $raw", e)
            }
        }

        val contentType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileName))

        return ImageItem.withDecodingNeeded(contentType, inputStream)
    }

    override fun supportedSchemes(): MutableCollection<String> {
        return mutableSetOf(SCHEME)
    }

    companion object {
        const val SCHEME: String = "file"

        /**
         * @see io.noties.markwon.image.destination.ImageDestinationProcessorAssets
         */
        fun createWithAssets(assetManager: AssetManager): FileSchemeHandler {
            return FileSchemeHandler(assetManager)
        }

        /**
         * @see .createWithAssets
         * @see io.noties.markwon.image.destination.ImageDestinationProcessorAssets
         *
         * @since 4.0.0
         */
        fun createWithAssets(context: Context): FileSchemeHandler {
            return FileSchemeHandler(context.assets)
        }

        fun create(): FileSchemeHandler {
            return FileSchemeHandler(null)
        }

        private const val FILE_ANDROID_ASSETS = "android_asset"
    }
}
