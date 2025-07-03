package io.noties.markwon.image

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import io.noties.markwon.image.DrawableUtils.applyIntrinsicBounds
import io.noties.markwon.image.ImagesPlugin.PlaceholderProvider
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

internal class AsyncDrawableLoaderImpl @VisibleForTesting constructor(
    builder: AsyncDrawableLoaderBuilder,
    private val handler: Handler
) : AsyncDrawableLoader() {
    private val executorService: ExecutorService? = builder.executorService
    private val schemeHandlers: MutableMap<String?, SchemeHandler?> = builder.schemeHandlers
    private val mediaDecoders: MutableMap<String?, MediaDecoder?> = builder.mediaDecoders
    private val defaultMediaDecoder: MediaDecoder? = builder.defaultMediaDecoder
    private val placeholderProvider: PlaceholderProvider? = builder.placeholderProvider
    private val errorHandler: ImagesPlugin.ErrorHandler?=builder.errorHandler


    // @since 4.0.0 use a hash-map with a AsyncDrawable as key for multiple requests
    //  for the same destination
    private val requests: MutableMap<AsyncDrawable?, Future<*>?> =
        HashMap(2)

    constructor(builder: AsyncDrawableLoaderBuilder) : this(
        builder,
        Handler(Looper.getMainLooper())
    )


    override fun load(drawable: AsyncDrawable) {
        val future = requests[drawable]
        if (future == null) {
            requests.put(drawable, execute(drawable))
        }
    }

    override fun cancel(drawable: AsyncDrawable) {
        val future = requests.remove(drawable)
        future?.cancel(true)

        handler.removeCallbacksAndMessages(drawable)
    }

    override fun placeholder(drawable: AsyncDrawable): Drawable? {
        return placeholderProvider?.providePlaceholder(drawable)
    }

    private fun execute(asyncDrawable: AsyncDrawable): Future<*> {
        return executorService!!.submit {
            val destination = asyncDrawable.destination

            val uri = destination.toUri()

            var drawable: Drawable? = null

            try {
                val scheme = uri.scheme
                check(
                    !(scheme == null
                            || scheme.isEmpty())
                ) { "No scheme is found: $destination" }

                // obtain scheme handler
                val schemeHandler = schemeHandlers[scheme]
                if (schemeHandler != null) {
                    // handle scheme

                    val imageItem = schemeHandler.handle(destination, uri)

                    // if resulting imageItem needs further decoding -> proceed
                    if (imageItem.hasDecodingNeeded()) {
                        val withDecodingNeeded = imageItem.asWithDecodingNeeded

                        // @since 4.6.2 close input stream
                        try {
                            var mediaDecoder =
                                mediaDecoders[withDecodingNeeded.contentType()]

                            if (mediaDecoder == null) {
                                mediaDecoder = defaultMediaDecoder
                            }

                            if (mediaDecoder != null) {
                                drawable = mediaDecoder.decode(
                                    withDecodingNeeded.contentType(),
                                    withDecodingNeeded.inputStream()
                                )
                            } else {
                                // throw that no media decoder is found
                                throw IllegalStateException("No media-decoder is found: $destination")
                            }
                        } finally {
                            try {
                                withDecodingNeeded.inputStream().close()
                            } catch (e: IOException) {
                                Log.e("MARKWON-IMAGE", "Error closing inputStream", e)
                            }
                        }
                    } else {
                        drawable = imageItem.asWithResult.result()
                    }
                } else {
                    // throw no scheme handler is available
                    throw IllegalStateException("No scheme-handler is found: $destination")
                }
            } catch (t: Throwable) {
                if (errorHandler != null) {
                    drawable = errorHandler.handleError(destination, t)
                } else {
                    // else simply log the error
                    Log.e("MARKWON-IMAGE", "Error loading image: $destination", t)
                }
            }

            val out = drawable

            // @since 4.0.0 apply intrinsic bounds (but only if they are empty)
            if (out != null) {
                val bounds = out.bounds
                if (bounds.isEmpty
                ) {
                    applyIntrinsicBounds(out)
                }
            }

            handler.postAtTime({ // validate that
                // * request was not cancelled
                // * out-result is present
                // * async-drawable is attached
                val future = requests.remove(asyncDrawable)
                if (future != null && out != null && asyncDrawable.isAttached) {
                    asyncDrawable.result = out
                }
            }, asyncDrawable, SystemClock.uptimeMillis())
        }
    }
}
