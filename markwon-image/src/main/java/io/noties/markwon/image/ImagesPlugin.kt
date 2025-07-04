package io.noties.markwon.image

import android.graphics.drawable.Drawable
import android.text.Spanned
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.image.AsyncDrawableScheduler.schedule
import io.noties.markwon.image.AsyncDrawableScheduler.unschedule
import org.commonmark.node.Image
import java.util.concurrent.ExecutorService

// @since 4.0.0
class ImagesPlugin @VisibleForTesting internal constructor(
    private val builder: AsyncDrawableLoaderBuilder
) : AbstractMarkwonPlugin() {
    /**
     * @since 4.0.0
     */
    interface ImagesConfigure {
        fun configureImages(plugin: ImagesPlugin)
    }

    /**
     * @since 4.0.0
     */
    interface PlaceholderProvider {
        fun providePlaceholder(drawable: AsyncDrawable): Drawable?
    }

    /**
     * @since 4.0.0
     */
    interface ErrorHandler {
        /**
         * Can optionally return a Drawable that will be displayed in case of an error
         */
        fun handleError(url: String, throwable: Throwable): Drawable?
    }

    // @since 4.0.0
    internal constructor() : this(AsyncDrawableLoaderBuilder())

    /**
     * Optional (by default new cached thread executor will be used)
     *
     * @since 4.0.0
     */
    fun executorService(executorService: ExecutorService): ImagesPlugin {
        builder.executorService(executorService)
        return this
    }

    /**
     * @see SchemeHandler
     *
     * @see io.noties.markwon.image.data.DataUriSchemeHandler
     *
     * @see io.noties.markwon.image.file.FileSchemeHandler
     *
     * @see io.noties.markwon.image.network.NetworkSchemeHandler
     *
     * @see io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
     *
     * @since 4.0.0
     */
    fun addSchemeHandler(schemeHandler: SchemeHandler): ImagesPlugin {
        builder.addSchemeHandler(schemeHandler)
        return this
    }

    /**
     * @see DefaultMediaDecoder
     *
     * @see io.noties.markwon.image.svg.SvgMediaDecoder
     *
     * @see io.noties.markwon.image.gif.GifMediaDecoder
     *
     * @since 4.0.0
     */
    fun addMediaDecoder(mediaDecoder: MediaDecoder): ImagesPlugin {
        builder.addMediaDecoder(mediaDecoder)
        return this
    }

    /**
     * Please note that if not specified a [DefaultMediaDecoder] will be used. So
     * if you need to disable default-image-media-decoder specify here own no-op implementation or null.
     *
     * @see DefaultMediaDecoder
     *
     * @since 4.0.0
     */
    fun defaultMediaDecoder(mediaDecoder: MediaDecoder?): ImagesPlugin {
        builder.defaultMediaDecoder(mediaDecoder)
        return this
    }

    /**
     * @since 4.0.0
     */
    fun removeSchemeHandler(scheme: String): ImagesPlugin {
        builder.removeSchemeHandler(scheme)
        return this
    }

    /**
     * @since 4.0.0
     */
    fun removeMediaDecoder(contentType: String): ImagesPlugin {
        builder.removeMediaDecoder(contentType)
        return this
    }

    /**
     * @since 4.0.0
     */
    fun placeholderProvider(placeholderProvider: PlaceholderProvider): ImagesPlugin {
        builder.placeholderProvider(placeholderProvider)
        return this
    }

    /**
     * @see ErrorHandler
     *
     * @since 4.0.0
     */
    fun errorHandler(errorHandler: ErrorHandler): ImagesPlugin {
        builder.errorHandler(errorHandler)
        return this
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.asyncDrawableLoader(this.builder.build())
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Image::class.java, ImageSpanFactory())
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        schedule(textView)
    }

    companion object {
        /**
         * Factory method to create an empty [ImagesPlugin] instance with no [SchemeHandler]s
         * nor [MediaDecoder]s registered. Can be used to further configure via instance methods or
         * via [MarkdownPlugin.configure]
         *
         * @see .create
         */
        @JvmStatic
        fun create(): ImagesPlugin {
            return ImagesPlugin()
        }

        fun create(configure: ImagesConfigure): ImagesPlugin {
            val plugin = ImagesPlugin()
            configure.configureImages(plugin)
            return plugin
        }
    }
}
