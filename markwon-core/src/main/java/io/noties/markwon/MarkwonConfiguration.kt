package io.noties.markwon

import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.ImageSizeResolver
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.destination.ImageDestinationProcessor
import io.noties.markwon.syntax.SyntaxHighlight
import io.noties.markwon.syntax.SyntaxHighlightNoOp

/**
 * since 3.0.0 renamed `SpannableConfiguration` -&gt; `MarkwonConfiguration`
 */
class MarkwonConfiguration private constructor(builder: Builder) {
    private val theme: MarkwonTheme
    private val asyncDrawableLoader: AsyncDrawableLoader?
    private val syntaxHighlight: SyntaxHighlight?
    private val linkResolver: LinkResolver?

    // @since 4.4.0
    private val imageDestinationProcessor: ImageDestinationProcessor?
    private val imageSizeResolver: ImageSizeResolver?

    // @since 3.0.0
    private val spansFactory: MarkwonSpansFactory

    init {
        this.theme = builder.theme!!
        this.asyncDrawableLoader = builder.asyncDrawableLoader
        this.syntaxHighlight = builder.syntaxHighlight
        this.linkResolver = builder.linkResolver
        this.imageDestinationProcessor = builder.imageDestinationProcessor
        this.imageSizeResolver = builder.imageSizeResolver
        this.spansFactory = builder.spansFactory!!
    }

    fun theme(): MarkwonTheme {
        return theme
    }

    fun asyncDrawableLoader(): AsyncDrawableLoader {
        return asyncDrawableLoader!!
    }

    fun syntaxHighlight(): SyntaxHighlight {
        return syntaxHighlight!!
    }

    fun linkResolver(): LinkResolver {
        return linkResolver!!
    }

    /**
     * @since 4.4.0
     */
    fun imageDestinationProcessor(): ImageDestinationProcessor {
        return imageDestinationProcessor!!
    }

    fun imageSizeResolver(): ImageSizeResolver {
        return imageSizeResolver!!
    }

    /**
     * @since 3.0.0
     */
    fun spansFactory(): MarkwonSpansFactory {
        return spansFactory
    }

    @Suppress("unused")
    class Builder internal constructor() {
        var theme: MarkwonTheme? = null
        var asyncDrawableLoader: AsyncDrawableLoader? = null
        var syntaxHighlight: SyntaxHighlight? = null
        var linkResolver: LinkResolver? = null

        // @since 4.4.0
        var imageDestinationProcessor: ImageDestinationProcessor? = null
        var imageSizeResolver: ImageSizeResolver? = null
        var spansFactory: MarkwonSpansFactory? = null

        /**
         * @since 4.0.0
         */
        fun asyncDrawableLoader(asyncDrawableLoader: AsyncDrawableLoader): Builder {
            this.asyncDrawableLoader = asyncDrawableLoader
            return this
        }

        fun syntaxHighlight(syntaxHighlight: SyntaxHighlight): Builder {
            this.syntaxHighlight = syntaxHighlight
            return this
        }

        fun linkResolver(linkResolver: LinkResolver): Builder {
            this.linkResolver = linkResolver
            return this
        }

        /**
         * @since 4.4.0
         */
        fun imageDestinationProcessor(imageDestinationProcessor: ImageDestinationProcessor): Builder {
            this.imageDestinationProcessor = imageDestinationProcessor
            return this
        }

        /**
         * @since 1.0.1
         */
        fun imageSizeResolver(imageSizeResolver: ImageSizeResolver): Builder {
            this.imageSizeResolver = imageSizeResolver
            return this
        }

        fun build(theme: MarkwonTheme, spansFactory: MarkwonSpansFactory): MarkwonConfiguration {
            this.theme = theme
            this.spansFactory = spansFactory

            // @since 4.0.0
            if (asyncDrawableLoader == null) {
                asyncDrawableLoader = AsyncDrawableLoader.noOp()
            }

            if (syntaxHighlight == null) {
                syntaxHighlight = SyntaxHighlightNoOp()
            }

            if (linkResolver == null) {
                linkResolver = LinkResolverDef()
            }

            // @since 4.4.0
            if (imageDestinationProcessor == null) {
                imageDestinationProcessor = ImageDestinationProcessor.noOp()
            }

            if (imageSizeResolver == null) {
                imageSizeResolver = ImageSizeResolverDef()
            }

            return MarkwonConfiguration(this)
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
