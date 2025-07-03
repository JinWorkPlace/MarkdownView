package io.noties.markwon.image

import io.noties.markwon.image.ImagesPlugin.PlaceholderProvider
import io.noties.markwon.image.data.DataUriSchemeHandler
import io.noties.markwon.image.gif.GifMediaDecoder
import io.noties.markwon.image.gif.GifSupport.hasGifSupport
import io.noties.markwon.image.network.NetworkSchemeHandler
import io.noties.markwon.image.svg.SvgMediaDecoder
import io.noties.markwon.image.svg.SvgSupport.hasSvgSupport
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class AsyncDrawableLoaderBuilder {
    @JvmField
    var executorService: ExecutorService? = null
    @JvmField
    val schemeHandlers: MutableMap<String?, SchemeHandler?> = HashMap(3)
    @JvmField
    val mediaDecoders: MutableMap<String?, MediaDecoder?> = HashMap(3)
    @JvmField
    var defaultMediaDecoder: MediaDecoder?
    @JvmField
    var placeholderProvider: PlaceholderProvider? = null
    @JvmField
    var errorHandler: ImagesPlugin.ErrorHandler? = null

    @JvmField
    var isBuilt: Boolean = false

    init {
        // @since 4.0.0
        // okay, let's add supported schemes at the start, this would be : data-uri and default network
        // we should not use file-scheme as it's a bit complicated to assume file usage (lack of permissions)

        addSchemeHandler(DataUriSchemeHandler.create())
        addSchemeHandler(NetworkSchemeHandler.create())

        // add SVG and GIF, but only if they are present in the class-path
        if (hasSvgSupport()) {
            addMediaDecoder(SvgMediaDecoder.create())
        }

        if (hasGifSupport()) {
            addMediaDecoder(GifMediaDecoder.create())
        }

        defaultMediaDecoder = DefaultMediaDecoder.create()
    }

    fun executorService(executorService: ExecutorService) {
        checkState()
        this.executorService = executorService
    }

    fun addSchemeHandler(schemeHandler: SchemeHandler) {
        checkState()
        for (scheme in schemeHandler.supportedSchemes()) {
            schemeHandlers.put(scheme, schemeHandler)
        }
    }

    fun addMediaDecoder(mediaDecoder: MediaDecoder) {
        checkState()
        for (type in mediaDecoder.supportedTypes()) {
            mediaDecoders.put(type, mediaDecoder)
        }
    }

    fun defaultMediaDecoder(mediaDecoder: MediaDecoder?) {
        checkState()
        this.defaultMediaDecoder = mediaDecoder
    }

    fun removeSchemeHandler(scheme: String) {
        checkState()
        schemeHandlers.remove(scheme)
    }

    fun removeMediaDecoder(contentType: String) {
        checkState()
        mediaDecoders.remove(contentType)
    }

    /**
     * @since 3.0.0
     */
    fun placeholderProvider(placeholderDrawableProvider: PlaceholderProvider) {
        checkState()
        this.placeholderProvider = placeholderDrawableProvider
    }

    /**
     * @since 3.0.0
     */
    fun errorHandler(errorHandler: ImagesPlugin.ErrorHandler) {
        checkState()
        this.errorHandler = errorHandler
    }

    fun build(): AsyncDrawableLoader {
        checkState()

        isBuilt = true

        if (executorService == null) {
            executorService = Executors.newCachedThreadPool()
        }

        return AsyncDrawableLoaderImpl(this)
    }

    private fun checkState() {
        check(!isBuilt) {
            "ImagesPlugin has already been configured " +
                    "and cannot be modified any further"
        }
    }
}
