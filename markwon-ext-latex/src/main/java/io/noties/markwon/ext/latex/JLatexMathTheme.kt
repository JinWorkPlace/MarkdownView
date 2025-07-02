package io.noties.markwon.ext.latex

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import ru.noties.jlatexmath.JLatexMathDrawable

/**
 * @since 4.3.0
 */
abstract class JLatexMathTheme {
    /**
     * Moved from [JLatexMathPlugin] in `4.3.0` version
     *
     * @since 4.0.0
     */
    interface BackgroundProvider {
        fun provide(): Drawable
    }

    /**
     * Special immutable class to hold padding information
     */
    class Padding(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        override fun toString(): String {
            return "Padding{left=$left, top=$top, right=$right, bottom=$bottom}"
        }

        companion object {
            fun all(value: Int): Padding {
                return Padding(value, value, value, value)
            }

            fun symmetric(vertical: Int, horizontal: Int): Padding {
                return Padding(horizontal, vertical, horizontal, vertical)
            }

            /**
             * @since 4.5.0
             */
            fun of(left: Int, top: Int, right: Int, bottom: Int): Padding {
                return Padding(left, top, right, bottom)
            }
        }
    }

    /**
     * @return text size in pixels for **inline LaTeX**
     * @see .blockTextSize
     */
    @Px
    abstract fun inlineTextSize(): Float

    /**
     * @return text size in pixels for **block LaTeX**
     * @see .inlineTextSize
     */
    @Px
    abstract fun blockTextSize(): Float

    abstract fun inlineBackgroundProvider(): BackgroundProvider?

    abstract fun blockBackgroundProvider(): BackgroundProvider?

    /**
     * @return boolean if **block LaTeX** must fit the width of canvas
     */
    abstract fun blockFitCanvas(): Boolean

    /**
     * @return horizontal alignment of **block LaTeX** if [.blockFitCanvas]
     * is enabled (thus space for alignment is available)
     */
    @JLatexMathDrawable.Align
    abstract fun blockHorizontalAlignment(): Int

    abstract fun inlinePadding(): Padding?

    abstract fun blockPadding(): Padding?

    @ColorInt
    abstract fun inlineTextColor(): Int

    @ColorInt
    abstract fun blockTextColor(): Int

    @Suppress("unused")
    class Builder internal constructor(
        val textSize: Float, val inlineTextSize: Float, val blockTextSize: Float
    ) {
        var backgroundProvider: BackgroundProvider? = null
        var inlineBackgroundProvider: BackgroundProvider? = null
        var blockBackgroundProvider: BackgroundProvider? = null

        var blockFitCanvas = true

        // horizontal alignment (when there is additional horizontal space)
        var blockHorizontalAlignment = JLatexMathDrawable.ALIGN_CENTER

        var padding: Padding? = null
        var inlinePadding: Padding? = null
        var blockPadding: Padding? = null

        var textColor = 0
        var inlineTextColor = 0
        var blockTextColor = 0

        fun backgroundProvider(backgroundProvider: BackgroundProvider?): Builder {
            this.backgroundProvider = backgroundProvider
            this.inlineBackgroundProvider = backgroundProvider
            this.blockBackgroundProvider = backgroundProvider
            return this
        }

        fun inlineBackgroundProvider(inlineBackgroundProvider: BackgroundProvider?): Builder {
            this.inlineBackgroundProvider = inlineBackgroundProvider
            return this
        }

        fun blockBackgroundProvider(blockBackgroundProvider: BackgroundProvider?): Builder {
            this.blockBackgroundProvider = blockBackgroundProvider
            return this
        }

        /**
         * Configure if `LaTeX` formula should take all available widget width.
         * By default - `true`
         */
        fun blockFitCanvas(blockFitCanvas: Boolean): Builder {
            this.blockFitCanvas = blockFitCanvas
            return this
        }

        fun blockHorizontalAlignment(@JLatexMathDrawable.Align blockHorizontalAlignment: Int): Builder {
            this.blockHorizontalAlignment = blockHorizontalAlignment
            return this
        }

        fun padding(padding: Padding?): Builder {
            this.padding = padding
            this.inlinePadding = padding
            this.blockPadding = padding
            return this
        }

        fun inlinePadding(inlinePadding: Padding?): Builder {
            this.inlinePadding = inlinePadding
            return this
        }

        fun blockPadding(blockPadding: Padding?): Builder {
            this.blockPadding = blockPadding
            return this
        }

        fun textColor(@ColorInt textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        fun inlineTextColor(@ColorInt inlineTextColor: Int): Builder {
            this.inlineTextColor = inlineTextColor
            return this
        }

        fun blockTextColor(@ColorInt blockTextColor: Int): Builder {
            this.blockTextColor = blockTextColor
            return this
        }

        fun build(): JLatexMathTheme {
            return Impl(this)
        }
    }

    internal class Impl(builder: Builder) : JLatexMathTheme() {
        private val textSize: Float = builder.textSize
        private val inlineTextSize: Float = builder.inlineTextSize
        private val blockTextSize: Float = builder.blockTextSize

        private val backgroundProvider: BackgroundProvider? = builder.backgroundProvider
        private val inlineBackgroundProvider: BackgroundProvider? = builder.inlineBackgroundProvider
        private val blockBackgroundProvider: BackgroundProvider? = builder.blockBackgroundProvider

        private val blockFitCanvas: Boolean = builder.blockFitCanvas

        // horizontal alignment (when there is additional horizontal space)
        private val blockHorizontalAlignment: Int = builder.blockHorizontalAlignment

        private val padding: Padding? = builder.padding
        private val inlinePadding: Padding? = builder.inlinePadding
        private val blockPadding: Padding? = builder.blockPadding

        private val textColor: Int = builder.textColor
        private val inlineTextColor: Int = builder.inlineTextColor
        private val blockTextColor: Int = builder.blockTextColor

        override fun inlineTextSize(): Float {
            if (inlineTextSize > 0f) {
                return inlineTextSize
            }
            return textSize
        }

        override fun blockTextSize(): Float {
            if (blockTextSize > 0f) {
                return blockTextSize
            }
            return textSize
        }

        override fun inlineBackgroundProvider(): BackgroundProvider? {
            if (inlineBackgroundProvider != null) {
                return inlineBackgroundProvider
            }
            return backgroundProvider
        }

        override fun blockBackgroundProvider(): BackgroundProvider? {
            if (blockBackgroundProvider != null) {
                return blockBackgroundProvider
            }
            return backgroundProvider
        }

        override fun blockFitCanvas(): Boolean {
            return blockFitCanvas
        }

        override fun blockHorizontalAlignment(): Int {
            return blockHorizontalAlignment
        }

        override fun inlinePadding(): Padding? {
            if (inlinePadding != null) {
                return inlinePadding
            }
            return padding
        }

        override fun blockPadding(): Padding? {
            if (blockPadding != null) {
                return blockPadding
            }
            return padding
        }

        override fun inlineTextColor(): Int {
            if (inlineTextColor != 0) {
                return inlineTextColor
            }
            return textColor
        }

        override fun blockTextColor(): Int {
            if (blockTextColor != 0) {
                return blockTextColor
            }
            return textColor
        }
    }

    companion object {
        fun create(@Px textSize: Float): JLatexMathTheme {
            return builder(textSize).build()
        }

        fun create(@Px inlineTextSize: Float, @Px blockTextSize: Float): JLatexMathTheme {
            return builder(inlineTextSize, blockTextSize).build()
        }

        fun builder(@Px textSize: Float): Builder {
            return Builder(textSize, 0f, 0f)
        }

        fun builder(@Px inlineTextSize: Float, @Px blockTextSize: Float): Builder {
            return Builder(0f, inlineTextSize, blockTextSize)
        }
    }
}
