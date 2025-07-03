package io.noties.markwon.ext.latex

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Spanned
import android.util.Log
import android.widget.TextView
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.AsyncDrawableSpan
import io.noties.markwon.image.DrawableUtils
import io.noties.markwon.image.ImageSizeResolver
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import org.commonmark.parser.Parser
import ru.noties.jlatexmath.JLatexMathDrawable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * @since 3.0.0
 */
class JLatexMathPlugin internal constructor(@JvmField @field:VisibleForTesting val config: Config) :
    AbstractMarkwonPlugin() {
    /**
     * @since 4.3.0
     */
    interface ErrorHandler {
        /**
         * @param latex that caused the error
         * @param error occurred
         * @return (optional) error drawable that will be used instead (if drawable will have bounds
         * it will be used, if not intrinsic bounds will be set)
         */
        fun handleError(latex: String, error: Throwable): Drawable?
    }

    interface BuilderConfigure {
        fun configureBuilder(builder: Builder)
    }

    @VisibleForTesting
    class Config(builder: Builder) {
        val theme: JLatexMathTheme = builder.theme.build()

        // @since 4.3.0
        @JvmField
        val blocksEnabled: Boolean = builder.blocksEnabled

        @JvmField
        val blocksLegacy: Boolean = builder.blocksLegacy

        @JvmField
        val inlinesEnabled: Boolean = builder.inlinesEnabled

        // @since 4.3.0
        val errorHandler: ErrorHandler? = builder.errorHandler

        val executorService: ExecutorService?

        init {
            // @since 4.0.0
            var executorService = builder.executorService
            if (executorService == null) {
                executorService = Executors.newCachedThreadPool()
            }
            this.executorService = executorService
        }
    }

    private val jLatextAsyncDrawableLoader: JLatextAsyncDrawableLoader =
        JLatextAsyncDrawableLoader(config)
    private val jLatexBlockImageSizeResolver: JLatexBlockImageSizeResolver =
        JLatexBlockImageSizeResolver(config.theme.blockFitCanvas())
    private val inlineImageSizeResolver: ImageSizeResolver

    init {
        this.inlineImageSizeResolver = InlineImageSizeResolver()
    }

    override fun configure(registry: MarkwonPlugin.Registry) {
        if (config.inlinesEnabled) {
            registry.require(MarkwonInlineParserPlugin::class.java).factoryBuilder()
                .addInlineProcessor(JLatexMathInlineProcessor())
        }
    }

    override fun configureParser(builder: Parser.Builder) {
        // @since 4.3.0
        if (config.blocksEnabled) {
            if (config.blocksLegacy) {
                builder.customBlockParserFactory(JLatexMathBlockParserLegacy.Factory())
            } else {
                builder.customBlockParserFactory(JLatexMathBlockParser.Factory())
            }
        }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        addBlockVisitor(builder)
        addInlineVisitor(builder)
    }

    private fun addBlockVisitor(builder: MarkwonVisitor.Builder) {
        if (!config.blocksEnabled) {
            return
        }

        builder.on(
            JLatexMathBlock::class.java, object : MarkwonVisitor.NodeVisitor<JLatexMathBlock> {
                override fun visit(visitor: MarkwonVisitor, node: JLatexMathBlock) {
                    visitor.blockStart(node)

                    val latex = node.latex()

                    val length = visitor.length()

                    // @since 4.0.2 we cannot append _raw_ latex as a placeholder-text,
                    // because Android will draw formula for each line of text, thus
                    // leading to formula duplicated (drawn on each line of text)
                    visitor.builder().append(prepareLatexTextPlaceholder(latex!!))

                    val configuration = visitor.configuration()

                    val span: AsyncDrawableSpan = JLatexAsyncDrawableSpan(
                        configuration.theme(), JLatextAsyncDrawable(
                            latex,
                            jLatextAsyncDrawableLoader,
                            jLatexBlockImageSizeResolver,
                            null,
                            true
                        ), config.theme.blockTextColor()
                    )

                    visitor.setSpans(length, span)

                    visitor.blockEnd(node)
                }
            })
    }

    private fun addInlineVisitor(builder: MarkwonVisitor.Builder) {
        if (!config.inlinesEnabled) {
            return
        }

        builder.on(
            JLatexMathNode::class.java, object : MarkwonVisitor.NodeVisitor<JLatexMathNode> {
                override fun visit(visitor: MarkwonVisitor, node: JLatexMathNode) {
                    val latex = node.latex()

                    val length = visitor.length()

                    // @since 4.0.2 we cannot append _raw_ latex as a placeholder-text,
                    // because Android will draw formula for each line of text, thus
                    // leading to formula duplicated (drawn on each line of text)
                    visitor.builder().append(prepareLatexTextPlaceholder(latex!!))

                    val configuration = visitor.configuration()

                    val span: AsyncDrawableSpan = JLatexInlineAsyncDrawableSpan(
                        configuration.theme(), JLatextAsyncDrawable(
                            latex, jLatextAsyncDrawableLoader, inlineImageSizeResolver, null, false
                        ), config.theme.inlineTextColor()
                    )

                    visitor.setSpans(length, span)
                }
            })
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        AsyncDrawableScheduler.unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        AsyncDrawableScheduler.schedule(textView)
    }

    @Suppress("unused")
    class Builder internal constructor(
        val theme: JLatexMathTheme.Builder
    ) {
        // @since 4.3.0
        var blocksEnabled = true
        var blocksLegacy = false
        var inlinesEnabled = false

        // @since 4.3.0
        var errorHandler: ErrorHandler? = null

        // @since 4.0.0
        var executorService: ExecutorService? = null

        fun theme(): JLatexMathTheme.Builder {
            return theme
        }

        /**
         * @since 4.3.0
         */
        fun blocksEnabled(blocksEnabled: Boolean): Builder {
            this.blocksEnabled = blocksEnabled
            return this
        }

        /**
         * @param blocksLegacy indicates if blocks should be handled in legacy mode (`pre 4.3.0`)
         * @since 4.3.0
         */
        fun blocksLegacy(blocksLegacy: Boolean): Builder {
            this.blocksLegacy = blocksLegacy
            return this
        }

        /**
         * @param inlinesEnabled indicates if inline parsing should be enabled.
         * NB, this requires `MarkwonInlineParserPlugin` to be used when creating `MarkwonInstance`
         * @since 4.3.0
         */
        fun inlinesEnabled(inlinesEnabled: Boolean): Builder {
            this.inlinesEnabled = inlinesEnabled
            return this
        }

        fun errorHandler(errorHandler: ErrorHandler?): Builder {
            this.errorHandler = errorHandler
            return this
        }

        /**
         * @since 4.0.0
         */
        fun executorService(executorService: ExecutorService): Builder {
            this.executorService = executorService
            return this
        }

        fun build(): Config {
            return Config(this)
        }
    }

    // @since 4.0.0
    internal class JLatextAsyncDrawableLoader(private val config: Config) : AsyncDrawableLoader() {
        private val handler = Handler(Looper.getMainLooper())
        private val cache: MutableMap<AsyncDrawable, Future<*>> = HashMap(3)

        override fun load(drawable: AsyncDrawable) {
            // this method must be called from main-thread only (thus synchronization can be skipped)

            // check for currently running tasks associated with provided drawable

            val future = cache[drawable]

            // if it's present -> proceed with new execution
            // as asyncDrawable is immutable, it won't have destination changed (so there is no need
            // to cancel any started tasks)
            if (future == null) {
                cache.put(drawable, config.executorService!!.submit(object : Runnable {
                    override fun run() {
                        // @since 4.0.1 wrap in try-catch block and add error logging
                        try {
                            execute()
                        } catch (t: Throwable) {
                            // @since 4.3.0 add error handling
                            val errorHandler = config.errorHandler
                            if (errorHandler == null) {
                                // as before
                                Log.e(
                                    "JLatexMathPlugin",
                                    "Error displaying latex: `" + drawable.destination + "`",
                                    t
                                )
                            } else {
                                // just call `getDestination` without casts and checks
                                val errorDrawable = errorHandler.handleError(
                                    drawable.destination, t
                                )
                                if (errorDrawable != null) {
                                    DrawableUtils.applyIntrinsicBoundsIfEmpty(errorDrawable)
                                    setResult(drawable, errorDrawable)
                                }
                            }
                        }
                    }

                    fun execute() {
                        val jLatextAsyncDrawable = drawable as JLatextAsyncDrawable
                        val jLatexMathDrawable: JLatexMathDrawable =
                            if (jLatextAsyncDrawable.isBlock) {
                                createBlockDrawable(jLatextAsyncDrawable)
                            } else {
                                createInlineDrawable(jLatextAsyncDrawable)
                            }

                        setResult(drawable, jLatexMathDrawable)
                    }
                }))
            }
        }

        override fun cancel(drawable: AsyncDrawable) {
            // this method also must be called from main thread only

            val future = cache.remove(drawable)
            future?.cancel(true)

            // remove all callbacks (via runnable) and messages posted for this drawable
            handler.removeCallbacksAndMessages(drawable)
        }

        override fun placeholder(drawable: AsyncDrawable): Drawable? {
            return null
        }

        // @since 4.3.0
        private fun createBlockDrawable(drawable: JLatextAsyncDrawable): JLatexMathDrawable {
            val latex = drawable.destination

            val theme = config.theme

            val backgroundProvider = theme.blockBackgroundProvider()
            val padding = theme.blockPadding()
            val color = theme.blockTextColor()

            val builder = JLatexMathDrawable.builder(latex).textSize(theme.blockTextSize())
                .align(theme.blockHorizontalAlignment())

            if (backgroundProvider != null) {
                builder.background(backgroundProvider.provide())
            }

            if (padding != null) {
                builder.padding(padding.left, padding.top, padding.right, padding.bottom)
            }

            if (color != 0) {
                builder.color(color)
            }

            return builder.build()
        }

        // @since 4.3.0
        private fun createInlineDrawable(drawable: JLatextAsyncDrawable): JLatexMathDrawable {
            val latex = drawable.destination

            val theme = config.theme

            val backgroundProvider = theme.inlineBackgroundProvider()
            val padding = theme.inlinePadding()
            val color = theme.inlineTextColor()

            val builder = JLatexMathDrawable.builder(latex).textSize(theme.inlineTextSize())

            if (backgroundProvider != null) {
                builder.background(backgroundProvider.provide())
            }

            if (padding != null) {
                builder.padding(padding.left, padding.top, padding.right, padding.bottom)
            }

            if (color != 0) {
                builder.color(color)
            }

            return builder.build()
        }

        // @since 4.3.0
        private fun setResult(drawable: AsyncDrawable, result: Drawable) {
            // we must post to handler, but also have a way to identify the drawable
            // for which we are posting (in case of cancellation)
            handler.postAtTime({ // remove entry from cache (it will be present if task is not cancelled)
                if (cache.remove(drawable) != null && drawable.isAttached) {
                    drawable.result = result
                }
            }, drawable, SystemClock.uptimeMillis())
        }
    }

    private class InlineImageSizeResolver : ImageSizeResolver() {
        override fun resolveImageSize(drawable: AsyncDrawable): Rect {
            // @since 4.4.0 resolve inline size (scale down if exceed available width)

            val imageBounds = drawable.result!!.bounds
            val canvasWidth = drawable.lastKnownCanvasWidth
            val w = imageBounds.width()

            if (w > canvasWidth) {
                // here we must scale it down (keeping the ratio)
                val ratio = w.toFloat() / imageBounds.height()
                val h = (canvasWidth / ratio + .5f).toInt()
                return Rect(0, 0, canvasWidth, h)
            }

            return imageBounds
        }
    }

    companion object {
        @JvmStatic
        fun create(textSize: Float): JLatexMathPlugin {
            return JLatexMathPlugin(builder(textSize).build())
        }

        /**
         * @since 4.3.0
         */
        fun create(@Px inlineTextSize: Float, @Px blockTextSize: Float): JLatexMathPlugin {
            return JLatexMathPlugin(builder(inlineTextSize, blockTextSize).build())
        }

        fun create(config: Config): JLatexMathPlugin {
            return JLatexMathPlugin(config)
        }

        @JvmStatic
        fun create(@Px textSize: Float, builderConfigure: BuilderConfigure): JLatexMathPlugin {
            val builder: Builder = builder(textSize)
            builderConfigure.configureBuilder(builder)
            return JLatexMathPlugin(builder.build())
        }

        /**
         * @since 4.3.0
         */
        fun create(
            @Px inlineTextSize: Float, @Px blockTextSize: Float, builderConfigure: BuilderConfigure
        ): JLatexMathPlugin {
            val builder: Builder = builder(inlineTextSize, blockTextSize)
            builderConfigure.configureBuilder(builder)
            return JLatexMathPlugin(builder.build())
        }

        fun builder(@Px textSize: Float): Builder {
            return Builder(JLatexMathTheme.builder(textSize))
        }

        /**
         * @since 4.3.0
         */
        fun builder(@Px inlineTextSize: Float, @Px blockTextSize: Float): Builder {
            return Builder(JLatexMathTheme.builder(inlineTextSize, blockTextSize))
        }

        // @since 4.0.2
        @JvmStatic
        @VisibleForTesting
        fun prepareLatexTextPlaceholder(latex: String): String {
            return latex.replace('\n', ' ').trim { it <= ' ' }
        }
    }
}
